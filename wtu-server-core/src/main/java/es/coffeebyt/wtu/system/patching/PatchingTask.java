package es.coffeebyt.wtu.system.patching;

import java.util.List;
import java.util.concurrent.Callable;

public interface PatchingTask extends Callable<List<PatchingResult>> { }
