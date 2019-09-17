package es.coffeebyt.wtu.system.patching.tasks;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.system.patching.PatchingResult;
import es.coffeebyt.wtu.system.patching.PatchingTask;
import es.coffeebyt.wtu.voucher.Voucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static es.coffeebyt.wtu.time.TimeUtil.twoYearsFromNowMillis;
import static es.coffeebyt.wtu.voucher.listeners.MaltaPromotion.EXPIRATION_TIME;
import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class CalculateMissingExpiresAtInVouchers implements PatchingTask {

    private final VoucherRepository voucherRepository;

    /**
     * - migrate old voucher expiration dates with priority:
     * - Purchased ones: expire in 2 years from now.
     * - Published ones: expire in 2 years from now.
     */
    @Override
    public List<PatchingResult> call() {
        List<Voucher> correctedVouchers = voucherRepository.findByPublishedTrue().stream()
                .filter(voucher -> voucher.getExpiresAt() == 0)
                .map(voucher -> voucher.withExpiresAt(twoYearsFromNowMillis()))
                .collect(toList());

        voucherRepository.saveAll(correctedVouchers);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z");

        return correctedVouchers.stream()
                .map(voucher -> new PatchingResult(
                        "Voucher.expiresAt",
                        format("Code: %s, sku: %s", voucher.getCode(), voucher.getSku()),
                        format("Updated missing 'expiresAt' value to %d (%s)",
                                voucher.getExpiresAt(),
                                ZonedDateTime.ofInstant(Instant.ofEpochMilli(EXPIRATION_TIME), UTC).format(formatter)
                        )
                ))
                .collect(toList());
    }
}
