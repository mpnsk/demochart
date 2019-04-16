package com.example;

import javax.servlet.annotation.WebServlet;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
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


        final Bound bound = new Bound();
        BeanItem<Bound> beanItem = new BeanItem<>(bound);

        final Property<Float> integerProperty = (Property<Float>) beanItem.getItemProperty("value");
        final TextField lowerBoundTextField = new TextField("lower bound", integerProperty);
        final TextField upperBoundTextField = new TextField("lower bound", integerProperty);


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
        int baseSliderInitValue = max - (chartRange / 2);
        baseSlider.setValue((double) baseSliderInitValue);
        baseSlider.setResolution(1);
        baseSlider.setWidth("100%");

        Slider rangeSlider = new Slider("range", 0, chartRange);
        rangeSlider.setValue((double) (chartRange / 2));
        rangeSlider.setImmediate(true);
        rangeSlider.setResolution(1);
        rangeSlider.setWidth("100%");
        DataSeriesItem topLeft = new DataSeriesItem(0, -4);
        DataSeriesItem topRight = new DataSeriesItem(0, 4);
        DataSeriesItem bottomLeft = new DataSeriesItem(9, 4);
        DataSeriesItem bottomRight = new DataSeriesItem(9, -4);
        DataSeries polygon = new DataSeries();
        Property.ValueChangeListener listener = event -> {
            float base = baseSlider.getValue().floatValue();
            float range = rangeSlider.getValue().floatValue() / 2;

            topLeft.setY(base - range);
            topRight.setY(base + range);
            bottomLeft.setY(base + range);
            bottomRight.setY(base - range);

//            int rangeLeft = chartRange - Math.abs(base);
            float rangeLeft = Math.min(Math.abs(min - base), Math.abs(max - base));
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
        HorizontalLayout horizontalLayout = new HorizontalLayout(baseSlider, rangeSlider);
        horizontalLayout.setWidth("100%");
        layout.addComponents(horizontalLayout);

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


    private Chart initChart(DataSeries polygon) {
        Chart chart = new Chart(ChartType.AREA);

        Configuration conf = chart.getConfiguration();
        PlotOptionsPolygon optionsPolygon = new PlotOptionsPolygon();
        double opacity = 0.5;
        optionsPolygon.setColor(new SolidColor(0, 0, 224, opacity));
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


}
