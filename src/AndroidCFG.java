
import java.util.Collections;

import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;

public class AndroidCFG {
    private static boolean SOOT_INITIALIZED=false;
    private final static String androidJAR="./lib/android30.jar";
    private final static String appApk="./1.apk";
    private static final String altClassPathOptionName = "alt-class-path";
    private static final String graphTypeOptionName = "graph-type";
//    private static final String defaultGraph = "BriefUnitGraph";
    private static final String defaultGraph = "BriefBlockGraph";
    private static final String irOptionName = "ir";
    private static final String defaultIR = "jimple";
    private static final String multipageOptionName = "multipages";
    private static final String briefLabelOptionName = "brief";
    public static void initialiseSoot()
    {
        if(SOOT_INITIALIZED)
            return;
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);
        Options.v().set_process_multiple_dex(true);

        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Collections.singletonList(appApk));
        Options.v().set_force_android_jar(androidJAR);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_soot_classpath(androidJAR);
        Scene.v().loadNecessaryClasses();

        SOOT_INITIALIZED=true;
    }
    public static void main(String[] args)
    {
        initialiseSoot();
        Transform tmp = new Transform("jtp.myAnalysis", new MyAnalysis());
        tmp.setDeclaredOptions("enabled " + altClassPathOptionName + ' ' + graphTypeOptionName + ' '
                + irOptionName + ' ' + multipageOptionName + ' ' + briefLabelOptionName + ' ');
        tmp.setDefaultOptions("enabled " + altClassPathOptionName + ": " + graphTypeOptionName + ':'
                + defaultGraph + ' ' + irOptionName + ':' + defaultIR + ' ' + multipageOptionName + ":false " + ' '
                + briefLabelOptionName + ":false ");
        PackManager.v().getPack("jtp").add(tmp);
        PackManager.v().runPacks();
        PackManager.v().writeOutput();
    }
}

