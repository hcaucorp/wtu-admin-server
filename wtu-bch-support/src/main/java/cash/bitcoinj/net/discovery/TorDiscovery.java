/*
 * Copyright 2014 Miron Cuperman
 * Copyright 2015 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cash.bitcoinj.net.discovery;

import static cash.bitcoinj.core.Peer.defaultExecutor;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singleton;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.subgraph.orchid.Circuit;
import com.subgraph.orchid.RelayCell;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.data.exitpolicy.ExitTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cash.bitcoinj.core.NetworkParameters;
import cash.bitcoinj.utils.ContextPropagatingThreadFactory;

/**
 * <p>Supports peer discovery through Tor.</p>
 *
 * <p>Failure to obtain at least four different peers through different exit nodes will cause
 * a PeerDiscoveryException will be thrown during getPeers().
 * </p>
 *
 * <p>DNS seeds do not attempt to enumerate every peer on the network. If you want more peers
 * to connect to, you need to discover them via other means (like addr broadcasts).</p>
 */
public class TorDiscovery implements PeerDiscovery {
    public static final int MINIMUM_ROUTER_COUNT = 4;
    public static final int ROUTER_LOOKUP_COUNT = 10;
    public static final int MINIMUM_ROUTER_LOOKUP_COUNT = 6;
    public static final int RECEIVE_RETRIES = 3;
    public static final int RESOLVE_STREAM_ID = 0x1000; // An arbitrary stream ID
    public static final int RESOLVE_CNAME = 0x00;
    public static final int RESOLVE_ERROR = 0xf0;
    public static final int RESOLVE_IPV4 = 0x04;
    public static final int RESOLVE_IPV6 = 0x06;
    private static final Logger log = LoggerFactory.getLogger(TorDiscovery.class);
    private final String[] hostNames;
    private final NetworkParameters netParams;
    private final CircuitPathChooser pathChooser;
    private final TorClient torClient;
    private ListeningExecutorService threadPool;

    /**
     * Supports finding peers through Tor. Community run DNS entry points will be used.
     *
     * @param netParams Network parameters to be used for port information.
     */
    public TorDiscovery(NetworkParameters netParams, TorClient torClient) {
        this(netParams.getDnsSeeds(), netParams, torClient);
    }

    /**
     * Supports finding peers through Tor.
     *
     * @param hostNames Host names to be examined for seed addresses.
     * @param netParams Network parameters to be used for port information.
     * @param torClient an already-started Tor client.
     */
    public TorDiscovery(String[] hostNames, NetworkParameters netParams, TorClient torClient) {
        this.hostNames = hostNames;
        this.netParams = netParams;

        this.torClient = torClient;
        this.pathChooser = CircuitPathChooser.create(torClient.getConfig(), torClient.getDirectory());
    }

    @Override
    public InetSocketAddress[] getPeers(long services, long timeoutValue, TimeUnit timeoutUnit) throws PeerDiscoveryException {
        if (hostNames == null)
            throw new PeerDiscoveryException("Unable to find any peers via DNS");
        if (services != 0)
            throw new PeerDiscoveryException("DNS seeds cannot filter by services: " + services);

        Set<Router> routers = Sets.newHashSet();
        ArrayList<ExitTarget> dummyTargets = Lists.newArrayList();

        // Collect exit nodes until we have enough
        while (routers.size() < ROUTER_LOOKUP_COUNT) {
            Router router = pathChooser.chooseExitNodeForTargets(dummyTargets);
            routers.add(router);
        }

        try {
            List<Circuit> circuits =
                    getCircuits(torClient.getConfig().getCircuitBuildTimeout(), TimeUnit.MILLISECONDS, routers);
            if (circuits.isEmpty())
                throw new PeerDiscoveryException("Failed to open any circuit within " +
                        timeoutValue + " " + timeoutUnit);

            Collection<InetSocketAddress> addresses = lookupAddresses(timeoutValue, timeoutUnit, circuits);

            if (addresses.size() < MINIMUM_ROUTER_COUNT)
                throw new PeerDiscoveryException("Unable to find enough peers via Tor - got " + addresses.size());
            ArrayList<InetSocketAddress> addressList = Lists.newArrayList();
            addressList.addAll(addresses);
            Collections.shuffle(addressList);
            return addressList.toArray(new InetSocketAddress[0]);
        } catch (InterruptedException e) {
            throw new PeerDiscoveryException(e);
        }
    }

    private List<Circuit> getCircuits(long timeoutValue, TimeUnit timeoutUnit, Set<Router> routers) throws InterruptedException {
        checkArgument(routers.size() >= MINIMUM_ROUTER_LOOKUP_COUNT, "Set of {} routers is smaller than required minimum {}",
                routers.size(), MINIMUM_ROUTER_LOOKUP_COUNT);
        createThreadPool(routers.size());

        try {
            List<ListenableFuture<Circuit>> circuitFutures = Lists.newArrayList();
            final CountDownLatch doneSignal = new CountDownLatch(MINIMUM_ROUTER_LOOKUP_COUNT);
            for (final Router router : routers) {
                ListenableFuture<Circuit> openCircuit = threadPool.submit(new Callable<Circuit>() {
                    @Override
                    public Circuit call() throws Exception {
                        return torClient.getCircuitManager().openInternalCircuitTo(Lists.newArrayList(router));
                    }
                });
                Futures.addCallback(openCircuit, new FutureCallback<Circuit>() {
                    @Override
                    public void onSuccess(Circuit circuit) {
                        doneSignal.countDown();
                    }

                    @Override
                    public void onFailure(Throwable thrown) {
                        doneSignal.countDown();
                    }
                }, defaultExecutor);
                circuitFutures.add(openCircuit);
            }

            boolean countedDown = doneSignal.await(timeoutValue, timeoutUnit);

            try {
                List<Circuit> circuits = new ArrayList<>(Futures.successfulAsList(circuitFutures).get());
                // Any failures will result in null entries.  Remove them.
                circuits.removeAll(singleton(null));
                int failures = routers.size() - circuits.size();
                if (failures > 0) log.warn("{} failures " + (countedDown ? "" : "(including timeout) ") +
                        "opening DNS lookup circuits", failures);
                return circuits;
            } catch (ExecutionException e) {
                // Cannot happen, successfulAsList accepts failures
                throw new RuntimeException(e);
            }
        } finally {
            shutdownThreadPool();
        }
    }

    private Collection<InetSocketAddress> lookupAddresses(long timeoutValue, TimeUnit timeoutUnit, List<Circuit> circuits) throws InterruptedException {
        createThreadPool(circuits.size() * hostNames.length);

        try {
            List<ListenableFuture<Lookup>> lookupFutures = Lists.newArrayList();
            for (final Circuit circuit : circuits) {
                for (final String seed : hostNames) {
                    lookupFutures.add(threadPool.submit(() -> new Lookup(circuit.getFinalCircuitNode().getRouter(), lookup(circuit, seed))));
                }
            }

            threadPool.awaitTermination(timeoutValue, timeoutUnit);
            int timeouts = 0;
            for (ListenableFuture<Lookup> future : lookupFutures) {
                if (!future.isDone()) {
                    timeouts++;
                    future.cancel(true);
                }
            }
            if (timeouts > 0)
                log.warn("{} DNS lookups timed out", timeouts);

            try {
                List<Lookup> lookups = new ArrayList<>(Futures.successfulAsList(lookupFutures).get());
                // Any failures will result in null entries.  Remove them.
                lookups.removeAll(singleton(null));

                // Use a map to enforce one result per exit node
                // TODO: randomize result selection better
                Map<HexDigest, InetSocketAddress> lookupMap = Maps.newHashMap();

                for (Lookup lookup : lookups) {
                    InetSocketAddress address = new InetSocketAddress(lookup.address, netParams.getPort());
                    lookupMap.put(lookup.router.getIdentityHash(), address);
                }

                return lookupMap.values();
            } catch (ExecutionException e) {
                // Cannot happen, successfulAsList accepts failures
                throw new RuntimeException(e);
            }
        } finally {
            shutdownThreadPool();
        }
    }

    private synchronized void shutdownThreadPool() {
        threadPool.shutdownNow();
        threadPool = null;
    }

    private synchronized void createThreadPool(int size) {
        threadPool = MoreExecutors.listeningDecorator(
                Executors.newFixedThreadPool(size, new ContextPropagatingThreadFactory("Tor DNS discovery")));
    }

    private InetAddress lookup(Circuit circuit, String seed) throws UnknownHostException {
        // Send a resolve cell to the exit node
        RelayCell cell = circuit.createRelayCell(RelayCell.RELAY_RESOLVE, RESOLVE_STREAM_ID, circuit.getFinalCircuitNode());
        cell.putString(seed);
        circuit.sendRelayCell(cell);

        // Wait a few cell timeout periods (3 * 20 sec) for replies, in case the path is slow
        for (int i = 0; i < RECEIVE_RETRIES; i++) {
            RelayCell res = circuit.receiveRelayCell();
            if (res != null) {
                while (res.cellBytesRemaining() > 0) {
                    int type = res.getByte();
                    int len = res.getByte();
                    byte[] value = new byte[len];
                    res.getByteArray(value);
                    int ttl = res.getInt();

                    if (type == RESOLVE_CNAME || type >= RESOLVE_ERROR) {
                        // TODO handle .onion CNAME replies
                        throw new RuntimeException(new String(value));
                    } else if (type == RESOLVE_IPV4 || type == RESOLVE_IPV6) {
                        return InetAddress.getByAddress(value);
                    }
                }
                break;
            }
        }
        throw new RuntimeException("Could not look up " + seed);
    }

    @Override
    public synchronized void shutdown() {
        if (threadPool != null) {
            shutdownThreadPool();
        }
    }

    private static class Lookup {
        final Router router;
        final InetAddress address;

        Lookup(Router router, InetAddress address) {
            this.router = router;
            this.address = address;
        }
    }
}
