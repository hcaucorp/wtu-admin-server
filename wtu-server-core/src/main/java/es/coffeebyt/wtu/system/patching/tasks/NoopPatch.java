package es.coffeebyt.wtu.system.patching.tasks;

import es.coffeebyt.wtu.system.patching.PatchingResult;
import es.coffeebyt.wtu.system.patching.PatchingTask;
import org.springframework.stereotype.Component;
import java.util.List;

import static java.util.Collections.singletonList;

@Component
public class NoopPatch implements PatchingTask {
    @Override public List<PatchingResult> call() {
        return singletonList(
                new PatchingResult("No entry changed", "none", "This is demo patch. No changes made.")
        );
    }
}
