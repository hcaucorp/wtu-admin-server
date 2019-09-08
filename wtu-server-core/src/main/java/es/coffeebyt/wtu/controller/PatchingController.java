package es.coffeebyt.wtu.controller;

import es.coffeebyt.wtu.system.patching.PatchingResult;
import es.coffeebyt.wtu.system.patching.PatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RequestMapping("/support/patching")
@RestController
@RequiredArgsConstructor
public class PatchingController {

    private final PatchingService patchingService;

    @GetMapping("/run")
    public List<PatchingResult> runPatches() {
        return patchingService.call();
    }

}
