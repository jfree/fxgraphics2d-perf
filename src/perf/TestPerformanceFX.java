/**
 * ----------------------
 * TestPerformanceFX.java
 * ----------------------
 *
 * Copyright (c) 2014, Object Refinery Limited.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the Object Refinery Limited nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL OBJECT REFINERY LIMITED BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Note that the above terms apply to this source file only, and not the 
 * Orson Charts library.
 * 
 */

package perf;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import com.orsoncharts.Chart3D;
import com.orsoncharts.Chart3DFactory;
import com.orsoncharts.Range;
import com.orsoncharts.axis.ValueAxis3D;
import com.orsoncharts.data.function.Function3D;
import com.orsoncharts.fx.Chart3DViewer;
import com.orsoncharts.graphics3d.Dimension3D;
import com.orsoncharts.graphics3d.ViewPoint3D;
import com.orsoncharts.plot.XYZPlot;
import com.orsoncharts.renderer.GradientColorScale;
import com.orsoncharts.renderer.xyz.SurfaceRenderer;
import static javafx.application.Application.launch;

/**
 * A program to measure the performance of rendering to the JavaFX Canvas via
 * FXGraphics2D.  See also TestPerformanceSwing.java.
 */
public class TestPerformanceFX extends Application {
    
    /** Number of charts to render in the warm-up phase. */
    private static final int WARMUP_ITERATIONS = 500;
    
    /** Number of charts to render in the performance measurement phase. */
    private static final int PERFORMANCE_ITERATIONS = 500;
    
    /**
     * Creates a surface chart for the demo.
     * 
     * @return A surface chart. 
     */
    public static Chart3D createDemoChart1() {
        Function3D function = new Function3D() {
            @Override
            public double getValue(double x, double z) {
                return Math.cos(x) * Math.sin(z);
            }
        };
        
        Chart3D chart = Chart3DFactory.createSurfaceChart(
                "SurfaceRendererDemo1", 
                "y = cos(x) * sin(z)", 
                function, "X", "Y", "Z");
 
        XYZPlot plot = (XYZPlot) chart.getPlot();
        plot.setDimensions(new Dimension3D(10, 5, 10));
        ValueAxis3D xAxis = plot.getXAxis();
        xAxis.setRange(-Math.PI, Math.PI);
        ValueAxis3D zAxis = plot.getZAxis();
        zAxis.setRange(-Math.PI, Math.PI);
        SurfaceRenderer renderer = (SurfaceRenderer) plot.getRenderer();
        renderer.setDrawFaceOutlines(false);
        renderer.setColorScale(new GradientColorScale(new Range(-1.0, 1.0), 
                Color.RED, Color.YELLOW));
        return chart;    
    }

    public static Chart3DViewer createDemoNode1() {
        Chart3D chart = createDemoChart1();
        Chart3DViewer viewer = new Chart3DViewer(chart);
        return viewer;
    }
    
    /**
     * A task that renders the chart once then, once done, submits a new task
     * for the next rendering.
     */
    static class DrawTask implements Runnable {
        private final int i;
        private int warmupCount;
        private int count;
        private Chart3DViewer viewer;
        private Map<String, Object> results;
        public DrawTask(int i, int warmupCount, int count, Chart3DViewer viewer,
                Map<String, Object> results) {
            this.i = i;
            this.warmupCount = warmupCount;
            this.count = count;
            this.viewer = viewer;
            this.results = results;
        }

        @Override
        public void run() {
            this.viewer.getChart().setViewPoint(new ViewPoint3D(i / 50.0, 
                    i / 100.0, 50, 0));
            this.viewer.getCanvas().draw();
            if (this.i == this.warmupCount - 1) {
                this.results.put("start", System.currentTimeMillis());
            }
            if (this.i == this.warmupCount + this.count - 1) {
                results.put("end", System.currentTimeMillis());
                System.out.println("start : " + this.results.get("start"));
                System.out.println("end : " + this.results.get("end"));
            } else {
                DrawTask next = new DrawTask(this.i + 1, this.warmupCount, 
                        this.count, this.viewer, this.results);
                Platform.runLater(next);
            }
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        // setup the app
        StackPane sp = new StackPane();
        final Chart3DViewer node = createDemoNode1();
        sp.getChildren().add(node);
        Scene scene = new Scene(sp, 768, 512);
        stage.setScene(scene);
        stage.setTitle("TestPerformanceFX.java");
        stage.show();
        
        // launch the performance measurement...
        Map<String, Object> results = new HashMap<>();
        System.out.println("begin warmup...");
        Platform.runLater(new DrawTask(0, WARMUP_ITERATIONS, 
                PERFORMANCE_ITERATIONS, node, results));
    }

    /**
     * Entry point.
     * 
     * @param args  ignored. 
     */
    public static void main(String[] args) {
        launch(args);
    }
}

