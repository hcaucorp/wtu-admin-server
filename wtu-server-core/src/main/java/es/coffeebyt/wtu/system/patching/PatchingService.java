package es.coffeebyt.wtu.system.patching;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatchingService implements Callable<List<PatchingResult>> {

    private final List<PatchingTask> patchingTasks;

    public List<PatchingResult> call() {
        return patchingTasks.stream()
                .map(this::callTaskErrorHandling)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private List<PatchingResult> callTaskErrorHandling(PatchingTask task) {
        try {
            return task.call();
        } catch (Exception e) {
            return singletonList(new PatchingResult("Error", null, e.getMessage()));
        }
    }
}
