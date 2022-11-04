import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.toolkits.graph.*;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.CompleteBlockGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.util.Chain;
import soot.util.HashChain;
import soot.util.cfgcmd.AltClassLoader;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.cfgcmd.CFGIntermediateRep;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;


public class MyAnalysis extends BodyTransformer {
    private CFGIntermediateRep ir;
    private CFGGraphType graphtype;
    private CFGToDotGraph drawer;

    private static final String altClassPathOptionName = "alt-class-path";
    private static final String graphTypeOptionName = "graph-type";
    private static final String defaultGraph = "BriefBlockGraph";
    private static final String irOptionName = "ir";
    private static final String defaultIR = "jimple";
    private static final String multipageOptionName = "multipages";
    private static final String briefLabelOptionName = "brief";

    protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
        initialize(options);
        graphtype = CFGGraphType.getGraphType("BriefBlockGraph");
        DirectedGraph<Unit> graph = graphtype.buildGraph(b);
//        System.out.println(graph);
        Body body = ir.getBody((JimpleBody) b);
        DotGraph canvas = graphtype.drawGraph(drawer, graph, body);
        String methodname = body.getMethod().getSubSignature();
//        String classname = body.getMethod().getDeclaringClass().getName().replaceAll("\\$", "\\.");
        String filename = soot.SourceLocator.v().getOutputDir();
        if (filename.length() > 0) {
            filename = filename + java.io.File.separator;
        }
        filename = filename  +methodname.replace(java.io.File.separatorChar, '.') + DotGraph.DOT_EXTENSION;
        G.v().out.println("Generate dot file in " + filename);
        canvas.plot(filename);
    }

    private void initialize(Map<String, String> options) {

        if (drawer == null) {
            drawer = new CFGToDotGraph();
            drawer.setBriefLabels(PhaseOptions.getBoolean(options, briefLabelOptionName));
            drawer.setOnePage(!PhaseOptions.getBoolean(options, multipageOptionName));
//            drawer.setUnexceptionalControlFlowAttr("color", "black");
//            drawer.setExceptionalControlFlowAttr("color", "red");
//            drawer.setExceptionEdgeAttr("color", "lightgray");
            drawer.setShowExceptions(Options.v().show_exception_dests());
            ir = CFGIntermediateRep.getIR(PhaseOptions.getString(options, irOptionName));
            graphtype = CFGGraphType.getGraphType(PhaseOptions.getString(options, graphTypeOptionName));

            AltClassLoader.v().setAltClassPath(PhaseOptions.getString(options, altClassPathOptionName));
            AltClassLoader.v().setAltClasses(
                    new String[] { "soot.toolkits.graph.ArrayRefBlockGraph", "soot.toolkits.graph.Block",
                            "soot.toolkits.graph.Block$AllMapTo", "soot.toolkits.graph.BlockGraph",
                            "soot.toolkits.graph.BriefBlockGraph", "soot.toolkits.graph.BriefUnitGraph",
                            "soot.toolkits.graph.CompleteBlockGraph", "soot.toolkits.graph.CompleteUnitGraph",
                            "soot.toolkits.graph.TrapUnitGraph", "soot.toolkits.graph.UnitGraph",
                            "soot.toolkits.graph.ZonedBlockGraph", });
        }
    }

}