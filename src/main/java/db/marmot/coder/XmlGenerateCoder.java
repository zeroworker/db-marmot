package db.marmot.coder;

import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.*;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.*;
import db.marmot.volume.ColumnVolume;
import db.marmot.volume.DataVolume;
import db.marmot.volume.Database;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * spring 运行环境
 * @author shaokang
 */
@Slf4j
public class XmlGenerateCoder {
	
	private static SchemaFactory schemaFactory;
	private static DocumentBuilder domBuilder;
	private static DataSourceRepository dataSourceRepository;
	public static final String VOLUME_XSD_PATH = "/META-INF/xsd/volume.xsd";
	public static final String STATISTICAL_XSD_PATH = "/META-INF/xsd/statistical.xsd";
	
	public XmlGenerateCoder(DataSourceRepository dataSourceRepository) {
		Validators.notNull(dataSourceRepository, "dataSourceRepository 不能为空");
		this.dataSourceRepository = dataSourceRepository;
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			this.domBuilder = builderFactory.newDocumentBuilder();
			this.schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		} catch (ParserConfigurationException e) {
			throw new CoderException("构建XML解析coder过程中出现错误", e);
		}
	}
	
	/**
	 * 解析统计模型配置文件
	 * @param location 相对路径 classpath:xxx/statistical.xml
	 */
	public void parseStatistical(String location) {
		try {
			Resource resource = new PathMatchingResourcePatternResolver().getResource(location);
			Schema schema = schemaFactory.newSchema(new ClassPathResource(STATISTICAL_XSD_PATH).getURL());
			schema.newValidator().validate(new StreamSource(resource.getURL().openStream()));
			Document document = domBuilder.parse(resource.getInputStream());
			StatisticalCreator.create(document.getDocumentElement());
		} catch (Exception e) {
			log.error("解析统计模型 xml文件出错", e);
		}
	}
	
	enum StatisticalCreator {
			model("model", new Builder() {
				@Override
				public StatisticalModel build(Element element) {
					return ModelCreator.create(element);
				}
				
				@Override
				public void store(StatisticalModel statisticalModel) {
					dataSourceRepository.storeStatisticalModel(statisticalModel);
				}
			});
		
		private String nodeName;
		private Builder builder;
		
		StatisticalCreator(String nodeName, Builder builder) {
			this.nodeName = nodeName;
			this.builder = builder;
		}
		
		public static void create(Element element) {
			Element nodeElement;
			Iterator iterator = new Iterator(element.getChildNodes());
			while ((nodeElement = iterator.nextElement()) != null) {
				for (StatisticalCreator statisticalCreator : values()) {
					if (statisticalCreator.nodeName.equals(nodeElement.getLocalName())) {
						statisticalCreator.builder.store(statisticalCreator.builder.build(nodeElement));
					}
				}
			}
		}
		
		public interface Builder {
			
			StatisticalModel build(Element element);
			
			void store(StatisticalModel statisticalModel);
		}
		
		enum ModelCreator {
				
				aggregate_columns("aggregateColumns") {
					@Override
					void setAttribute(StatisticalModelBuilder builder, Element element) {
						Element nodeElement;
						Iterator iterator = new Iterator(element.getChildNodes());
						while ((nodeElement = iterator.nextElement()) != null) {
							AggregateColumn aggregateColumn = new AggregateColumn();
							aggregateColumn.setColumnCode(nodeElement.getAttribute("columnCode"));
							aggregateColumn.setAggregates(Aggregates.getByCode(nodeElement.getAttribute("aggregates")));
							aggregateColumn.setColumnType(ColumnType.getByCode(nodeElement.getAttribute("columnType")));
							builder.addAggregateColumn(aggregateColumn);
						}
					}
				},
				
				condition_columns("conditionColumns") {
					@Override
					void setAttribute(StatisticalModelBuilder builder, Element element) {
						Element nodeElement;
						Iterator iterator = new Iterator(element.getChildNodes());
						while ((nodeElement = iterator.nextElement()) != null) {
							ConditionColumn conditionColumn = new ConditionColumn();
							conditionColumn.setColumnCode(nodeElement.getAttribute("columnCode"));
							conditionColumn.setColumnType(ColumnType.getByCode(nodeElement.getAttribute("columnType")));
							conditionColumn.setOperators(Operators.getByCode(nodeElement.getAttribute("operators")));
							conditionColumn.setRightValue(ConverterAdapter.getInstance().getColumnConverter(conditionColumn.getColumnType()).columnValueConvert(nodeElement.getAttribute("rightValue")));
							builder.addConditionColumn(conditionColumn);
						}
					}
				},
				
				group_columns("groupColumns") {
					@Override
					void setAttribute(StatisticalModelBuilder builder, Element element) {
						Element nodeElement;
						Iterator iterator = new Iterator(element.getChildNodes());
						while ((nodeElement = iterator.nextElement()) != null) {
							GroupColumn groupColumn = new GroupColumn();
							groupColumn.setColumnCode(nodeElement.getAttribute("columnCode"));
							groupColumn.setColumnType(ColumnType.getByCode(nodeElement.getAttribute("columnType")));
							builder.addGroupColumn(groupColumn);
						}
					}
				},
				
				direction_columns("directionColumns") {
					@Override
					void setAttribute(StatisticalModelBuilder builder, Element element) {
						Element nodeElement;
						Iterator iterator = new Iterator(element.getChildNodes());
						while ((nodeElement = iterator.nextElement()) != null) {
							DirectionColumn directionColumn = new DirectionColumn();
							directionColumn.setColumnCode(nodeElement.getAttribute("columnCode"));
							directionColumn.setColumnType(ColumnType.getByCode(nodeElement.getAttribute("columnType")));
							directionColumn.setOperators(Operators.getByCode(nodeElement.getAttribute("operators")));
							directionColumn.setRightValue(ConverterAdapter.getInstance().getColumnConverter(directionColumn.getColumnType()).columnValueConvert(nodeElement.getAttribute("rightValue")));
							builder.addDirectionColumn(directionColumn);
						}
					}
				};
			
			private String nodeName;
			
			ModelCreator(String nodeName) {
				this.nodeName = nodeName;
			}
			
			public static StatisticalModel create(Element element) {
				StatisticalModelBuilder builder = createByAttribute(element);
				setByChildNodes(builder, element.getChildNodes());
				return builder.builder();
			}
			
			public static StatisticalModelBuilder createByAttribute(Element element) {
				StatisticalModelBuilder builder = new StatisticalModelBuilder();
				builder.addMemo(element.getAttribute("memo"))
						.addModelName(element.getAttribute("modelName"))
						.addOffsetExpr(element.getAttribute("offsetExpr"))
						.addWindowLength(Integer.valueOf(element.getAttribute("windowLength")))
						.addWindowType(WindowType.getByCode(element.getAttribute("windowType")))
						.addWindowUnit(WindowUnit.getByCode(element.getAttribute("windowUnit")))
						.addDataVolume(dataSourceRepository.findDataVolume(element.getAttribute("volumeCode")));
				return builder;
			}
			
			public static void setByChildNodes(StatisticalModelBuilder builder, NodeList nodeList) {
				Element nodeElement;
				Iterator iterator = new Iterator(nodeList);
				while ((nodeElement = iterator.nextElement()) != null) {
					for (ModelCreator modelCreator : values()) {
						if (modelCreator.nodeName.equals(nodeElement.getLocalName())) {
							modelCreator.setAttribute(builder, nodeElement);
						}
					}
				}
			}
			
			abstract void setAttribute(StatisticalModelBuilder builder, Element element);
		}
	}
	
	/**
	 * 解析数据集配置文件
	 * @param location 相对路径 classpath:xxx/volume.xml
	 */
	public void parseVolume(String location) {
		try {
			Resource resource = new PathMatchingResourcePatternResolver().getResource(location);
			Schema schema = schemaFactory.newSchema(new ClassPathResource(VOLUME_XSD_PATH).getURL());
			schema.newValidator().validate(new StreamSource(resource.getURL().openStream()));
			Document document = domBuilder.parse(resource.getInputStream());
			VolumeCreator.create(document.getDocumentElement());
		} catch (Exception e) {
			log.error("解析数据集 xml文件出错", e);
		}
	}
	
	enum VolumeCreator {
			data_base("dataBase", new Builder<Database>() {
				@Override
				public Database build(Element element) {
					Database database = new Database();
					database.setName(element.getAttribute("name"));
					database.setUrl(element.getAttribute("url"));
					database.setUserName(element.getAttribute("userName"));
					database.setPassword(element.getAttribute("password"));
					return database;
				}
				
				@Override
				public void store(Database database) {
					dataSourceRepository.storeDatabase(database);
				}
			}),
			
			data_volume("dataVolume", new Builder<DataVolume>() {
				@Override
				public DataVolume build(Element element) {
					DataVolume dataVolume = new DataVolume();
					dataVolume.setVolumeName(element.getAttribute("volumeName"));
					dataVolume.setVolumeCode(element.getAttribute("volumeCode"));
					dataVolume.setVolumeType(VolumeType.getByCode(element.getAttribute("volumeType")));
					dataVolume.setDbName(element.getAttribute("dbName"));
					dataVolume.setSqlScript(element.getAttribute("sqlScript"));
					dataVolume.setVolumeLimit(Long.valueOf(element.getAttribute("volumeLimit")));
					dataVolume.setContent(element.getAttribute("content"));
					return dataVolume;
				}
				
				@Override
				public void store(DataVolume dataVolume) {
					dataSourceRepository.storeDataVolume(dataVolume);
				}
			}),
			
			column_volume("columnVolume", new Builder<ColumnVolume>() {
				@Override
				public ColumnVolume build(Element element) {
					ColumnVolume columnVolume = new ColumnVolume();
					columnVolume.setVolumeName(element.getAttribute("volumeName"));
					columnVolume.setVolumeCode(element.getAttribute("volumeCode"));
					columnVolume.setVolumeType(VolumeType.getByCode(element.getAttribute("volumeType")));
					columnVolume.setColumnCode(element.getAttribute("columnCode"));
					columnVolume.setDbName(element.getAttribute("dbName"));
					columnVolume.setColumnValueCode(element.getAttribute("columnValueCode"));
					columnVolume.setColumnShowCode(element.getAttribute("columnShowCode"));
					columnVolume.setScript(element.getAttribute("script"));
					columnVolume.setContent(element.getAttribute("content"));
					return columnVolume;
				}
				
				@Override
				public void store(ColumnVolume columnVolume) {
					dataSourceRepository.storeColumnVolume(columnVolume);
				}
			});
		
		private String nodeName;
		private Builder builder;
		
		VolumeCreator(String nodeName, Builder builder) {
			this.nodeName = nodeName;
			this.builder = builder;
		}
		
		public interface Builder<E> {
			
			E build(Element element);
			
			void store(E e);
		}
		
		public static void create(Element element) {
			Element nodeElement;
			Iterator iterator = new Iterator(element.getChildNodes());
			while ((nodeElement = iterator.nextElement()) != null) {
				for (VolumeCreator volumeCreator : values()) {
					if (volumeCreator.nodeName.equals(nodeElement.getLocalName())) {
						volumeCreator.builder.store(volumeCreator.builder.build(nodeElement));
					}
				}
			}
		}
	}
	
	public static class Iterator {
		
		private int index = -1;
		private NodeList nodeList;
		
		public Iterator(NodeList nodeList) {
			this.nodeList = nodeList;
		}
		
		public Element nextElement() {
			if (index < nodeList.getLength()) {
				index = index + 1;
				Node node = nodeList.item(index);
				if (node.getNodeType() == Element.ELEMENT_NODE) {
					return (Element) node;
				}
				return nextElement();
			}
			return null;
		}
	}
	
	class CoderException extends RuntimeException {
		public CoderException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
