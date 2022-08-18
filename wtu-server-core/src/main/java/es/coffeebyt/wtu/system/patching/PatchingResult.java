package es.coffeebyt.wtu.system.patching;

import lombok.Value;

@Value
public class PatchingResult {

    String patchedEntity;
    String id;
    String message;

}
