package com.example;

import javax.servlet.annotation.WebServlet;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();

        final TextField name = new TextField();
        name.setCaption("Type your name here:");

        Button button = new Button("Click Me!");
        button.addClickListener(e -> {
            layout.addComponent(new Label("Thanks " + name.getValue()
                    + ", it works!"));
        });
        int min = -4;
        int max = 4;
        int chartRange = max - min;
        Slider baseSlider = new Slider("base", min, max);
        baseSlider.setImmediate(true);
        int baseSliderInitValue = chartRange / 2;
        baseSlider.setValue((double) baseSliderInitValue);
        Slider rangeSlider = new Slider("range", 0, baseSliderInitValue);
        rangeSlider.setValue((double) (baseSliderInitValue /2));
        rangeSlider.setImmediate(true);
        DataSeriesItem topLeft = new DataSeriesItem(0,-4);
        DataSeriesItem topRight = new DataSeriesItem(0, 4);
        DataSeriesItem bottomLeft = new DataSeriesItem(9, 4);
        DataSeriesItem bottomRight = new DataSeriesItem(9, -4);
        DataSeries polygon = new DataSeries();
        Property.ValueChangeListener listener = event -> {
            int base = baseSlider.getValue().intValue();
            int range = rangeSlider.getValue().intValue();

            topLeft.setY(base - range);
            topRight.setY(base + range);
            bottomLeft.setY(base+range);
            bottomRight.setY(base - range);

//            int rangeLeft = chartRange - Math.abs(base);
            int rangeLeft = Math.min(Math.abs(min - base), Math.abs(max - base));
            rangeSlider.setMax(rangeLeft);

            polygon.update(topLeft);
            polygon.update(topRight);
            polygon.update(bottomLeft);
            polygon.update(bottomRight);

            System.out.println("base = " + base);
            System.out.println("range = " + range);
            System.out.println("rangeLeft = " + rangeLeft);

        };
        baseSlider.addValueChangeListener(listener);
        rangeSlider.addValueChangeListener(listener);
        layout.addComponents(baseSlider, rangeSlider);

//        layout.addComponents(name, button);
        layout.setMargin(true);
        layout.setSpacing(true);

        setContent(layout);
//        Slider slider = new Slider(1, 100);
//        slider.setImmediate(true);
//        layout.addComponent(slider);

        polygon.add(topLeft);
        polygon.add(topRight);
        polygon.add(bottomLeft);
        polygon.add(bottomRight);
//        BeanItemContainer<MyPoint2D> integerBeanItemContainer = new BeanItemContainer<>(MyPoint2D.class);
//        MyPoint2D topLeft = new MyPoint2D(0, 4);
//        integerBeanItemContainer.addBean(topLeft);
//        integerBeanItemContainer.addBean(new MyPoint2D(0, -4));
//        integerBeanItemContainer.addBean(new MyPoint2D(8, -4));
//        integerBeanItemContainer.addBean(new MyPoint2D(8, 4));
        IndexedContainer indexedContainer = new IndexedContainer();
//        indexedContainer.addItem()

        Chart chart = initChart(polygon);

        layout.addComponent(chart);
//        layout.addComponent(otherChart());
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }


    Chart areaChart() {
        Chart chart = new Chart(ChartType.AREASPLINE);
        chart.setHeight("450px");

        Configuration conf = chart.getConfiguration();

        conf.setTitle(new Title("Average fruit consumption during one week"));

        Legend legend = new Legend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setAlign(HorizontalAlign.LEFT);
        legend.setFloating(true);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setX(150);
        legend.setY(100);
        conf.setLegend(legend);

        XAxis xAxis = new XAxis();
        xAxis.setCategories(new String[]{"Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"});
        // add blue background for the weekend
        PlotBand plotBand = new PlotBand(4.5, 6.5, SolidColor.BLUE);
        plotBand.setZIndex(1);
        xAxis.setPlotBands(plotBand);
        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle(new AxisTitle("Fruit units"));
        conf.addyAxis(yAxis);

        Tooltip tooltip = new Tooltip();
        // Customize tooltip formatting
        tooltip.setHeaderFormat("");
        tooltip.setPointFormat("{series.name}: {point.y} units");
        // Same could be achieved by defining following JS formatter funtion:
        // tooltip.setFormatter("function(){ return this.x +': '+ this.y +' units';}");
        // ... or its shorthand form:
        // tooltip.setFormatter("this.x +': '+ this.y +' units'");
        conf.setTooltip(tooltip);

        PlotOptionsArea plotOptions = new PlotOptionsArea();
        plotOptions.setFillOpacity(0.5);
        conf.setPlotOptions(plotOptions);

        ListSeries o = new ListSeries("John", 3, 4, 3, 5, 4, 10);
        // Add last value separately
        o.addData(12);
        conf.addSeries(o);
        conf.addSeries(new ListSeries("Jane", 1, 3, 4, 3, 3, 5, 4));

        chart.drawChart(conf);
        return chart;
    }

    private Chart initChart(DataSeries polygon) {
        Chart chart = new Chart(ChartType.AREA);

        Configuration conf = chart.getConfiguration();
        PlotOptionsPolygon optionsPolygon = new PlotOptionsPolygon();
        double opacity = 0.5;
        optionsPolygon.setColor(new SolidColor(255, 255, 224, opacity));
//        optionsPolygon.setEnableMouseTracking(false);
        polygon.setPlotOptions(optionsPolygon);
        polygon.setName("Target");

        conf.addSeries(polygon);

        conf.getChart().setInverted(true);

        conf.setTitle("Auswertung");

        conf.setExporting(true);
        conf.getExporting().setWidth(800);

        String[] categoriesY = {
                "Soziale Unterstützung: Stand. Wert",
                "Beziehungsorientierung: Stand. Wert",
                "Emotionale Stabilität: Stand. Wert",
                "Selbstwirksamkeit: Stand. Wert",
                "Motivation: Hoffnung auf Erfolg\nvs. Angst vor Misserfolg: Stand. Wert",
                "Gewissenhaftigkeit: Stand. Wert",
                "Verträglichkeit: Stand. Wert",
                "Psych. Wohlbefinden - Stimmung: Stand. Wert",
                "Psych. Wohlbefinden -\nPsych. Belastbarkeit: Stand. Wert",
                "Impulsivität - Besonnenheit: Stand. Wert"
        };

        XAxis x = new XAxis();
        x.setCategories(categoriesY);
        AxisTitle title = new AxisTitle("Skala / Ausprägung");
        title.setAlign(VerticalAlign.TOP);
        x.setTitle(title);
        conf.addxAxis(x);

        Legend legend = new Legend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setAlign(HorizontalAlign.RIGHT);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setX(-100);
        legend.setY(100);
        legend.setFloating(true);
        legend.setBorderWidth(1);
        legend.setBackgroundColor(new SolidColor("#FFFFFF"));
        legend.setShadow(true);
        conf.setLegend(legend);

//        conf.disableCredits();
        DataSeries series = new DataSeries();
        PlotOptionsSpline splinePlotOptions = new PlotOptionsSpline();
        splinePlotOptions.setStacking(Stacking.NORMAL);
        Marker marker = new Marker();
        marker.setLineWidth(2);
        marker.setLineColor(new SolidColor("black"));
        marker.setFillColor(new SolidColor("white"));
        splinePlotOptions.setMarker(marker);
        splinePlotOptions.setColor(new SolidColor("black"));
        splinePlotOptions.setAllowPointSelect(false);
        series.setPlotOptions(splinePlotOptions);
        series.add(new DataSeriesItem(categoriesY[0], 3.2));
        series.add(new DataSeriesItem(categoriesY[1], -3.2));
        series.add(new DataSeriesItem(categoriesY[2], 0.5));
        series.add(new DataSeriesItem(categoriesY[3], 3.1));
        series.add(new DataSeriesItem(categoriesY[4], 2.5));
        series.add(new DataSeriesItem(categoriesY[5], -0.5));
        series.add(new DataSeriesItem(categoriesY[6], 2.3));
        series.add(new DataSeriesItem(categoriesY[7], 3.3));
        series.add(new DataSeriesItem(categoriesY[8], 3.3));
        series.add(new DataSeriesItem(categoriesY[9], 3.3));
        conf.addSeries(series);

//        PlotOptionsAreasplinerange plotOptions = new PlotOptionsAreasplinerange();
//        plotOptions.setPointStart(-4);
//        plotOptions.setStacking(Stacking.NORMAL);
//        RangeSeries range = new RangeSeries("range", new Integer[]{-4, +4}, new Integer[]{-4,2}, new Integer[]{-5,5});
//        range.setPlotOptions(plotOptions);
////        areaSeries.add();
//        DataSeries areaSeries = new DataSeries();
//        areaSeries.setPlotOptions(plotOptions);
//        areaSeries.add(new DataSeriesItem("series1", 4.3));
//        areaSeries.add(new DataSeriesItem("series2", -4.3));
//        conf.addSeries(range);


        chart.drawChart(conf);

        return chart;
    }

    Chart otherChart() {
        Chart chart = new Chart(ChartType.AREA);

        Configuration conf = chart.getConfiguration();

        conf.setTitle("Combined Chart");

        conf.setExporting(true);

        XAxis x = new XAxis();
        x.setCategories(new String[]{"Apples", "Oranges", "Pears", "Bananas",
                "Plums"});
        conf.addxAxis(x);

        conf.setLabels(new HTMLLabels(new HTMLLabelItem(
                "Total fruit consumption")));

        DataSeries series = new DataSeries();
        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        series.setPlotOptions(plotOptions);
        series.setName("Jane");
        series.setData(3, 2, 1, 3, 4);
        conf.addSeries(series);

        series = new DataSeries();
        plotOptions = new PlotOptionsColumn();
        series.setPlotOptions(plotOptions);
        series.setName("John");
        series.setData(2, 3, 5, 7, 6);
        conf.addSeries(series);

        series = new DataSeries();
        plotOptions = new PlotOptionsColumn();
        series.setPlotOptions(plotOptions);
        series.setName("Joe");
        series.setData(4, 3, 3, 9, 0);
        conf.addSeries(series);

        series = new DataSeries();
        PlotOptionsSpline splinePlotOptions = new PlotOptionsSpline();
        series.setPlotOptions(splinePlotOptions);
        series.setName("Average");
        series.setData(3, 2.67, 3, 6.33, 3.33);
        conf.addSeries(series);

        series = new DataSeries();
        series.setPlotOptions(new PlotOptionsPie());
        series.setName("Total consumption");
        DataSeriesItem item = new DataSeriesItem("Jane", 13);
        series.add(item);
        item = new DataSeriesItem("John", 23);
        series.add(item);
        item = new DataSeriesItem("Joe", 19);
        series.add(item);

        PlotOptionsPie plotOptionsPie = new PlotOptionsPie();
        plotOptionsPie.setSize("100px");
        plotOptionsPie.setCenter("100px", "80px");
        plotOptionsPie.setShowInLegend(false);
        series.setPlotOptions(plotOptionsPie);
        conf.addSeries(series);

        return chart;
    }
}
