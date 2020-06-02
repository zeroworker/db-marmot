package db.marmot.converter;

import db.marmot.enums.Aggregates;
import db.marmot.enums.GraphicCycle;
import db.marmot.enums.Operators;
import db.marmot.graphic.converter.GraphicCycleConverter;

import java.util.Map;

/**
 * @author shaokang
 */
public abstract class AbstractSelectSqlBuilderConverter implements SelectSqlBuilderConverter{

    protected Map<Operators, OperatorsConverter> operatorsConverters;
    protected Map<Aggregates, AggregatesConverter> aggregatesConverters;
    protected Map<GraphicCycle, GraphicCycleConverter> graphicCycleConverters;

    @Override
    public void setOperatorsConverters(Map<Operators, OperatorsConverter> operatorsConverters) {
        this.operatorsConverters = operatorsConverters;
    }

    @Override
    public void setAggregatesConverters(Map<Aggregates, AggregatesConverter> aggregatesConverters) {
        this.aggregatesConverters = aggregatesConverters;
    }

    @Override
    public void setGraphicCycleConverters(Map<GraphicCycle, GraphicCycleConverter> graphicCycleConverters) {
        this.graphicCycleConverters = graphicCycleConverters;
    }
}
