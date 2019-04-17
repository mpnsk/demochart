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
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;

import java.util.ArrayList;
import java.util.List;

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


        double min = -3.2;
        double max = 3.3;
        double chartRange = max - min;
        double baseSliderInitValue = max - (chartRange / 2d);
        double rangeInitValue = (chartRange / 2d);

        Property<Double> baseProp = new ObjectProperty<>(0d);
        Property<Double> rangeProp = new ObjectProperty<>(0d);

        String baseCaption = "center";
        Slider baseSlider = new Slider(baseCaption, ((int) min), ((int) max));
        baseSlider.setImmediate(true);
        baseSlider.setResolution(1);
        baseSlider.setWidth("100%");
        baseSlider.setPropertyDataSource(baseProp);
        final TextField baseTextField = new TextField(baseCaption, baseProp);
        baseProp.setValue(baseSliderInitValue);

        String rangeCaption = "range";
        Slider rangeSlider = new Slider(rangeCaption, 0, ((int) chartRange));
        rangeSlider.setImmediate(true);
        rangeSlider.setResolution(1);
        rangeSlider.setWidth("100%");
        rangeSlider.setPropertyDataSource(rangeProp);
        rangeProp.setValue(rangeInitValue);

        final TextField rangeTextField = new TextField(rangeCaption, rangeProp);
        DataSeries polygon = new DataSeries();
        DataSeriesItem topLeft = new DataSeriesItem(0, -4);
        DataSeriesItem topRight = new DataSeriesItem(0, 4);
        DataSeriesItem bottomLeft = new DataSeriesItem(9, 4);
        DataSeriesItem bottomRight = new DataSeriesItem(9, -4);
        polygon.add(topLeft);
        polygon.add(topRight);
        polygon.add(bottomLeft);
        polygon.add(bottomRight);
        Property.ValueChangeListener listener = event -> {
            double base = baseProp.getValue();
            double range = rangeProp.getValue();

            topLeft.setY(base - range / 2);
            topRight.setY(base + range / 2);
            bottomLeft.setY(base + range / 2);
            bottomRight.setY(base - range / 2);

            double rangeLeft = Math.min(Math.abs(min - base), Math.abs(max - base)) * 2;
            rangeSlider.setMax(rangeLeft);

            polygon.update(topLeft);
            polygon.update(topRight);
            polygon.update(bottomLeft);
            polygon.update(bottomRight);

            System.out.println("base = " + base);
            System.out.println("range = " + range);
            System.out.println("rangeLeft = " + rangeLeft);

        };

        rangeTextField.setImmediate(true);
        rangeTextField.addValueChangeListener(listener);
        baseTextField.setImmediate(true);
        baseTextField.addValueChangeListener(listener);
        rangeSlider.setImmediate(true);
        rangeSlider.addValueChangeListener(listener);
        baseSlider.setImmediate(true);
        baseSlider.addValueChangeListener(listener);

        HorizontalLayout horizontalLayout = new HorizontalLayout(baseSlider, baseTextField, rangeSlider, rangeTextField);
        horizontalLayout.setWidth("100%");
        layout.addComponents(horizontalLayout);

        layout.setMargin(true);
        layout.setSpacing(true);

        setContent(layout);


        Chart chart = initChart(polygon);

        layout.addComponent(chart);
        listener.valueChange(null);
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
        polygon.setName("Zielbereich");

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
        legend.setY(0);
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
        return chart;
    }


}
