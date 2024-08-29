package natives.accuracy;

import com.ishland.c2me.opts.natives_math.common.ISATarget;
import com.ishland.c2me.opts.natives_math.common.NativeLoader;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;

public abstract class AbstractAccuracy {

    protected final ISATarget[] targets;
    protected final MethodHandle[] MHs;
    protected final int[] maxUlp;

    protected AbstractAccuracy(ISATarget[] targets, MethodHandle template, String prefix) {
        this.targets = Arrays.stream(targets).filter(ISATarget::isNativelySupported).toArray(ISATarget[]::new);
        this.MHs = Arrays.stream(this.targets)
                .map(isaTarget -> template.bindTo(NativeLoader.lookup.find(prefix + isaTarget.getSuffix()).get()))
                .toArray(MethodHandle[]::new);
        this.maxUlp = new int[this.targets.length];
    }

    protected static int ulpDistance(double original, double that) {
        int dist = 0;
        while (that > original) {
            that = Math.nextAfter(that, original);
            dist ++;
        }
        return dist;
    }

    protected void printUlps() {
        for (int i = 0; i < this.maxUlp.length; i++) {
            System.out.println(String.format("%s: max error %d ulps", this.targets[i], this.maxUlp[i]));
        }
    }

}
