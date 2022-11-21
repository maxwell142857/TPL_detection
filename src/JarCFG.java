
import java.io.File;
import java.io.IOException;
import java.util.*;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Transform;
import soot.Unit;
import soot.jimple.JimpleBody;
import soot.options.Options;
import soot.toolkits.graph.DirectedGraph;
import soot.util.cfgcmd.AltClassLoader;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.cfgcmd.CFGIntermediateRep;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;


public class JarCFG extends BodyTransformer {

    private static final String altClassPathOptionName = "alt-class-path";
    private static final String graphTypeOptionName = "graph-type";
    private static final String defaultGraph = "BriefBlockGraph";
    private static final String irOptionName = "ir";
    private static final String defaultIR = "jimple";
    private static final String multipageOptionName = "multipages";
    private static final String briefLabelOptionName = "brief";

    private CFGGraphType graphtype;
    private CFGIntermediateRep ir;
    private CFGToDotGraph drawer;

    protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
        initialize(options);
//        System.out.println(options);
//        System.out.println(b);
        Body body = ir.getBody((JimpleBody) b);
//        System.out.println(body);
        print_cfg(body);

    }

    public static void main(String[] args) throws IOException {
        JarCFG viewer = new JarCFG();
        Transform printTransform = new Transform("jtp.printcfg", viewer);
        printTransform.setDeclaredOptions("enabled " + altClassPathOptionName + ' ' + graphTypeOptionName + ' '
                + irOptionName + ' ' + multipageOptionName + ' ' + briefLabelOptionName + ' ');
        printTransform.setDefaultOptions("enabled " + altClassPathOptionName + ": " + graphTypeOptionName + ':'
                + defaultGraph + ' ' + irOptionName + ':' + defaultIR + ' ' + multipageOptionName + ":false " + ' '
                + briefLabelOptionName + ":false ");
        PackManager.v().getPack("jtp").add(printTransform);

        File[] files = new File("./target").listFiles();
        ArrayList<String> soot_args = new ArrayList<>();
//        String s = "animation-1.2.1";
//        File file = new File("./target/" + s);
        assert files != null;
        soot_args.add("-cp");
        soot_args.add(".;D:/soft/Java/jdk1.8.0_333/jre/lib;./lib/android30.jar;");
        soot_args.add("-allow-phantom-refs");
        soot_args.add("-process-dir");
        for (File file : files) {
            // 文件夹
            if (!file.isFile()) {
                soot_args.add("./target/" + file.getName());
                String[] soot_args_ = new String[soot_args.size()];
                soot_args.toArray(soot_args_);
                soot.Main.main((soot_args_));
                Serialization.main(new String[]{file.getName() + ".txt"});
                Main.deleteFile(new File("sootOutput"));
                soot_args.remove(soot_args.size() - 1);
            }
        }


    }

    private void initialize(Map<String, String> options) {
        if (drawer == null) {
            drawer = new CFGToDotGraph();
            drawer.setBriefLabels(PhaseOptions.getBoolean(options, briefLabelOptionName));
            drawer.setOnePage(!PhaseOptions.getBoolean(options, multipageOptionName));
            drawer.setUnexceptionalControlFlowAttr("color", "black");
            drawer.setExceptionalControlFlowAttr("color", "red");
            drawer.setExceptionEdgeAttr("color", "lightgray");
            drawer.setShowExceptions(Options.v().show_exception_dests());
            ir = CFGIntermediateRep.getIR(PhaseOptions.getString(options, irOptionName));
            graphtype = CFGGraphType.getGraphType(PhaseOptions.getString(options, graphTypeOptionName));

            AltClassLoader.v().setAltClassPath(PhaseOptions.getString(options, altClassPathOptionName));
            AltClassLoader.v().setAltClasses(
                    new String[]{"soot.toolkits.graph.ArrayRefBlockGraph", "soot.toolkits.graph.Block",
                            "soot.toolkits.graph.Block$AllMapTo", "soot.toolkits.graph.BlockGraph",
                            "soot.toolkits.graph.BriefBlockGraph", "soot.toolkits.graph.BriefUnitGraph",
                            "soot.toolkits.graph.CompleteBlockGraph", "soot.toolkits.graph.CompleteUnitGraph",
                            "soot.toolkits.graph.TrapUnitGraph", "soot.toolkits.graph.UnitGraph",
                            "soot.toolkits.graph.ZonedBlockGraph",});
        }
    }

    protected void print_cfg(Body body) {
        DirectedGraph<Unit> graph = graphtype.buildGraph(body);
        DotGraph canvas = graphtype.drawGraph(drawer, graph, body);
        String methodname = body.getMethod().getSubSignature();
//        String classname = body.getMethod().getDeclaringClass().getName().replaceAll("\\$", "\\.");
        String filename = soot.SourceLocator.v().getOutputDir();
        if (filename.length() > 0) {
            filename = filename + java.io.File.separator;
        }
        filename = filename + methodname.replace(java.io.File.separatorChar, '.') + DotGraph.DOT_EXTENSION;

//        G.v().out.println("Generate dot file in " + filename);
        canvas.plot(filename);
    }
}