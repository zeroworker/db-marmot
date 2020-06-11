import React from 'react';
import moment from 'moment';
import {Button, DatePicker, Form, Icon, Input, InputNumber, Menu, Message, Modal, Radio, Select, Tree} from 'antd';
import PropTypes from 'prop-types';
import {DropTarget} from 'react-dnd';
import ItemTypes from '../types';
import Order from './Order';

const {MonthPicker, RangePicker, WeekPicker} = DatePicker;
const {SubMenu} = Menu;
const TreeNode = Tree.TreeNode;

const style = {
  width: '100%',
  color: 'white',
  padding: '10px 0',
  textAlign: 'center',
  fontSize: '1rem',
  lineHeight: 'normal',
  border: '1px dashed rgba(255,255,255,0.2)'
}

const boxTarget = {
  // 当有对应的 drag source 放在当前组件区域时，会返回一个对象，可以在 monitor.getDropResult() 中获取到
  // drop: () => ({ name: 'Dustbin' })
  drop(props, monitor, component) {
    component.addItem(monitor.getItem());
  }
}

@DropTarget(
  // type 标识，这里是字符串 'box'
  ItemTypes.BOX,
  // 接收拖拽的事件对象
  boxTarget,
  // 收集功能函数，包含 connect 和 monitor 参数
  // connect 里面的函数用来将 DOM 节点与 react-dnd 的 backend 建立联系
  (connect, monitor) => ({
    // 包裹住 DOM 节点，使其可以接收对应的拖拽组件
    connectDropTarget: connect.dropTarget(),
    // drag source是否在 drop target 区域
    isOver: monitor.isOver(),
    // 是否可以被放置
    canDrop: monitor.canDrop()
  })
)
class Filter extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      treeContent: [],
      visibleNo: false,
      formLayout: 'inline',
      filterConditionStatus: false,
      conditionType: 'OR',
      visibleDate: false,
      visibleStartTime: false,
      timeType: 'year',
      startTimeData: '',
      endTimeType: '',
      endTimeData: '',
      isOpenStartYear: false,
      isOpenEndYear: false,
      startYear: null,
      endYear: null,
      startDate: null,
      endDate: null,
      endOpen: false,

      root: '<h1>hello</h1>',
    }
    this.handleClickMenu = this.handleClickMenu.bind(this);
    this.onChangeCondition = this.onChangeCondition.bind(this);
    this.loop = this.loop.bind(this);
    this.onDrop = this.onDrop.bind(this);
  }

  static propTypes = {
    canDrop: PropTypes.bool.isRequired,
    isOver: PropTypes.bool.isRequired,
    connectDropTarget: PropTypes.func.isRequired
  }

  componentDidMount() {
    this.props.onRef(this);
  }

  handleMeasureCondition() {
    console.log('handleMeasureCondition 子组件的方法被父组件调用');
    console.log('---treeContent---', this.state.treeContent);
  }

  // 新增Item
  addItem(Item) {
    console.log('---110---', Item);
    // 为新加Item设置新的key
    let {treeContent} = this.state;
    Item = this.setItemKeys(Item);
    if (!Item) return this.setState({treeContent});
    treeContent.push(Item);
    this.setState({treeContent});
  }

  setItemKeys(Item) {
    const itemIndex = this.state.treeContent.findIndex((value, index, arr) => {
      return value.name === Item.name;
    });
    if (itemIndex < 0) {
      // Item = Object.assign({}, Item, {key: Item.key + Date.now()});
      Item = Object.assign({}, Item);
      if (!Item.children || Item.children.length === 0) return Item;
    } else {
      console.log('---存在相同的字段条件---');
      return false;
    }
  }

  handleMore(data) {
    console.log('-----79------', data);
  }

  async handleDeleteMeasure(index) {
    console.log('-----75------', index);
    // this.props.handleDeleteDustbin(index);
    let treeContent = this.state.treeContent;
    treeContent.splice(index, 1);
    await this.setState({treeContent});
  }

  async handleClickMenu(data, title, index) {
    let treeContent = this.state.treeContent;
    treeContent[index].orderLable = title;
    treeContent[index].orderBy = data;
    await this.setState({treeContent});
  }

  loop(data, key, callback) {
    data.forEach((item, index, arr) => {
      if (item.name === key) return callback(item, index, arr);
      if (item.children) {
        return this.loop(item.children, key, callback);
      }
    })
  }

  async onDrop(info) {
    const dropKey = info.node.props.eventKey;
    const dragKey = info.dragNode ? info.dragNode.props.eventKey : null;
    const dropPos = info.node.props.pos.split('-');
    const dropPosition = info.dropPosition - Number(dropPos[dropPos.length - 1]);
    const data = [...this.state.treeContent];
    if (!dragKey) {
      this.setState({
        treeContent: data,
      });
      return;
    }
    // Find dragObject
    let dragObj
    // 保存节点信息并删除节点
    this.loop(data, dragKey, (item, index, arr) => {
      arr.splice(index, 1)
      dragObj = item
    });
    let ar;
    let i;
    this.loop(data, dropKey, (item, index, arr) => {
      ar = arr;
      i = index;
    });
    if (dropPosition === -1) {
      ar.splice(i, 0, dragObj);
    } else {
      ar.splice(i + 1, 0, dragObj);
    }
    this.setState({
      treeContent: data,
    })
  }

  async handleFilter(value, index) {
    console.log('-----185----', value, index);
    if (value.dataType === 'number') {
      await this.setState({
        visibleNo: true,
      });
    } else if (value.dataType === 'date') {
      await this.setState({
        visibleDate: true,
      });
    } else {
      Message.info('敬请期待');
    }
  }

  handleFilterStatus() {
    this.setState({
      filterConditionStatus: true
    })
  }

  onChangeCondition(e) {
    console.log('radio checked', e.target.value);
    this.setState({
      conditionType: e.target.value
    })
  }

  deleteFilterCondition() {
    this.setState({
      filterConditionStatus: false
    })
  }

  handleOk = e => {
    console.log(e);
    this.setState({
      visibleNo: false,
    });
  };

  handleCancel = e => {
    console.log(e);
    this.setState({
      visibleNo: false,
    });
  };

  handleOkDate = e => {
    console.log(e);
    this.setState({
      visibleDate: false,
    });
  };

  handleCancelDate = e => {
    console.log(e);
    this.setState({
      visibleDate: false,
    });
  };

  handleStartModal = () => {
    console.log('---238---');
    this.setState({
      visibleStartTime: true,
    });
  };

  handleOkMonth = e => {
    console.log(e);
    this.setState({
      visibleStartTime: false,
    });
  };

  handleCancelMonth = e => {
    console.log(e);
    this.setState({
      visibleStartTime: false,
    });
  };

  hanleStartOnchange = (value) => {
    console.log('---value---', value);
    this.setState({
      timeType: value
    })
  };

  handleChangeDate = (data) => {
    console.log('data: ', data);
    this.setState({
      startYear: data,
      isOpenStartYear: false
    })
  };

  disabledStartDate = startValue => {
    const {endDate} = this.state;
    if (!startValue || !endDate) return false;
    return startValue.valueOf() > endDate.valueOf();
  };

  disabledEndDate = endValue => {
    const {startDate} = this.state;
    if (!endValue || !startDate) return false;
    return endValue.valueOf() <= startDate.valueOf();
  };

  onChange = (field, value) => {
    this.setState({
      [field]: value,
    });
  };

  onStartChange = value => {
    this.onChange('startDate', value);
  };

  onEndChange = value => {
    this.onChange('endDate', value);
  };

  handleStartOpenChange = open => {
    if (!open) {
      this.setState({endOpen: true});
    }
  };

  handleEndOpenChange = open => {
    this.setState({endOpen: open});
  };

  // 由数组生成Tree
  getTreeNode(treeContent) {
    if (!treeContent || treeContent.length === 0) return null;
    let startTimeComponent = null,
      endTimeComponent = null;
    if (this.state.timeType === 'year') {
      startTimeComponent = <DatePicker
        value={this.state.startYear}
        open={this.state.isOpenStartYear}
        mode="year"
        placeholder="select year"
        format="YYYY"
        onOpenChange={(status) => {
          if (status) {
            this.setState({isOpenStartYear: true})
          } else {
            this.setState({isOpenStartYear: false})
          }
        }}
        onPanelChange={(v) => {
          console.log(v);
          this.setState({
            startYear: v,
            isOpenStartYear: false
          })
        }}
        onChange={() => {
          this.setState({startYear: null})
        }}
      />
      endTimeComponent = <DatePicker
        value={this.state.endYear}
        open={this.state.isOpenEndYear}
        mode="year"
        placeholder="select year"
        format="YYYY"
        onOpenChange={(status) => {
          if (status) {
            this.setState({isOpenEndYear: true})
          } else {
            this.setState({isOpenEndYear: false})
          }
        }}
        onPanelChange={(v) => {
          console.log(v);
          this.setState({
            endYear: v,
            isOpenEndYear: false
          })
        }}
        onChange={() => {
          this.setState({endYear: null})
        }}
      />
    } else if (this.state.timeType === 'month') {
      startTimeComponent = <MonthPicker placeholder="Select month"/>
      endTimeComponent = <MonthPicker placeholder="Select month"/>
    } else if (this.state.timeType === 'week') {
      startTimeComponent = <WeekPicker placeholder="Select Week"/>
      endTimeComponent = <WeekPicker placeholder="Select Week"/>
    } else if (this.state.timeType === 'day') {
      startTimeComponent = <DatePicker
        disabledDate={this.disabledStartDate}
        placeholder="Select Day"
        value={this.state.startDate}
        onChange={this.onStartChange}
      />
      endTimeComponent = <DatePicker
        disabledDate={this.disabledEndDate}
        placeholder="Select Day"
        value={this.state.endDate}
        onChange={this.onEndChange}
        open={this.state.endOpen}
        onOpenChange={this.handleEndOpenChange}
      />
    } else if (this.state.timeType === 'quarter') {
      startTimeComponent = <DatePicker
        mode="month"
        placeholder="select year"
        format="YYYY"
        monthCellContentRender={(date, local) => {
          console.log('---395----', date);
          console.log('---396----', local);
          let monthName = moment(date).format('M');

          if (['3', '6', '9', '12'].includes(monthName)) {
            return monthName;
          } else {
            return null;
          }

          // return (
          // 	<h1>第{Math.ceil(monthName / 3)}季度</h1>
          // )

          // let monthContent = null;
          // if ([3, 6, 9, 12].includes(monthName)) {
          // 	monthContent = (
          // 		<h1>季度</h1>
          // 	)
          // 	return monthContent;
          // } else {
          // 	return ('');
          // }


          // let tmpData = '123456';
          // return (
          // 	<h1>{monthName}</h1>
          // )
        }}
      />

      endTimeComponent = null;

    }

    const treeNode = treeContent.map((value, index) => {
      return (
        <TreeNode
          draggable
          title={
            <div className="fx fx-x-between fx-y-center">
              <div className="fx fx-x-between fx-y-center">
                {
                  value.dataType === 'number'
                    ?
                    <div>
                      <i className="icon-no"></i>
                      {value.name}({value.orderLable})
                    </div>
                    :
                    value.dataType === 'date'
                      ?
                      <div>
                        <Icon type="clock-circle"/>
                        {value.name}
                      </div>
                      :
                      <div>
                        <i className="icon-str"></i>
                        {value.name}
                      </div>
                }
              </div>
              <div className="fx fx-x-between fx-y-center operation-cont">
                {
                  value.type === 'MEASURE'
                    ?
                    <Order
                      index={index}
                      type={value.type}
                      handleClickMenu={this.handleClickMenu}/>
                    :
                    null
                }
                <Icon className="m-l-5 m-r-5" type="filter" onClick={() => this.handleFilter(value, index)}/>
                <Icon type="delete" onClick={() => this.handleDeleteMeasure(index)}/>
              </div>

              <Modal
                title="设置过滤器（数字类型）"
                okText="确定"
                cancelText="取消"
                visible={this.state.visibleNo}
                onOk={this.handleOk}
                onCancel={this.handleCancel}
              >
                <Form layout={this.state.formLayout}>
                  <Form.Item>
                    <Input value="hello" disabled/>
                  </Form.Item>
                  <Form.Item>
                    <Input value="hello" disabled/>
                  </Form.Item>
                  <Form.Item style={{width: '100%'}}>
                    <Radio.Group
                      name="radiogroup"
                      defaultValue='OR'
                      onChange={this.onChangeCondition}
                      value={this.state.conditionType}
                    >
                      <Radio value='OR'>或者</Radio>
                      <Radio value='AND'>且</Radio>
                    </Radio.Group>
                  </Form.Item>
                  <Form.Item style={{width: '100%'}}>
                    <div>
                      <Select defaultValue="&gt;" style={{width: 120}}>
                        <Option value="--">--</Option>
                        <Option value=">">&gt;</Option>
                        <Option value=">=">>=</Option>
                        <Option value="<">&lt;</Option>
                        <Option value="<=">&lt;=</Option>
                        <Option value="≠"> ≠ </Option>
                        <Option value="="> = </Option>
                      </Select>
                      <InputNumber style={{marginLeft: "15px"}}/>
                      <span style={{marginLeft: "5px"}}>
												{
                          !this.state.filterConditionStatus ? '' : this.state.conditionType === 'OR' ? '或者' : '且'
                        }
											</span>
                    </div>
                    <div className={this.state.filterConditionStatus ? '' : 'fn-hide'}>
                      <Select defaultValue="&gt;" style={{width: 120}}>
                        <Option value="--">--</Option>
                        <Option value=">">&gt;</Option>
                        <Option value=">=">>=</Option>
                        <Option value="<">&lt;</Option>
                        <Option value="<=">&lt;=</Option>
                        <Option value="≠"> ≠ </Option>
                        <Option value="="> = </Option>
                      </Select>
                      <InputNumber style={{marginLeft: "15px"}}/>
                      <Icon type="delete" style={{marginLeft: "10px"}} onClick={() => this.deleteFilterCondition()}/>
                    </div>
                  </Form.Item>
                  <Form.Item style={{width: '100%'}}>
                    <Button type="dashed" style={{width: '100%'}} disabled={this.state.filterConditionStatus} onClick={() => this.handleFilterStatus()}>
                      <Icon type="plus"/>新建筛选条件
                    </Button>
                  </Form.Item>
                </Form>
              </Modal>

              <Modal
                title="设置过滤器（时间类型）"
                okText="确定"
                cancelText="取消"
                visible={this.state.visibleDate}
                onOk={this.handleOkDate}
                onCancel={this.handleCancelDate}
              >
                <Form layout={this.state.formLayout}>
                  <Form.Item>
                    <Input value="hello" disabled style={{width: "120px"}}/>
                  </Form.Item>
                  <Form.Item>
                    <Input value="hello" disabled style={{width: "120px"}}/>
                  </Form.Item>
                  <Form.Item label="筛选类型" style={{width: "100%"}}>
                    <Select defaultValue="年" style={{width: 120}} onChange={this.hanleStartOnchange}>
                      <Option value="year">年</Option>
                      <Option value="month">月</Option>
                      <Option value="week">周</Option>
                      <Option value="day">日</Option>
                      <Option value="quarter">季度</Option>
                    </Select>
                  </Form.Item>
                  <Form.Item label="开始时间">
                    {startTimeComponent}
                  </Form.Item>
                  <Form.Item label="结束时间">
                    {endTimeComponent}
                  </Form.Item>
                </Form>
              </Modal>

            </div>
          }
          key={value.name}
        >
          {this.getTreeNode(value.children)}
        </TreeNode>
      )
    })
    return (
      treeNode
    )
  }

  render() {
    const {treeContent} = this.state;
    const treeNodes = this.getTreeNode(treeContent);
    const {canDrop, isOver, connectDropTarget} = this.props;
    const isActive = canDrop && isOver;
    let backgroundColor = 'rgba(70,140,255,.1)';
    // 拖拽组件此时正处于 drag target 区域时，当前组件背景色变为 darkgreen
    if (isActive) {
      backgroundColor = 'rgba(255,255,255,0.2)';
    }
    // 当前组件可以放置 drag source 时，背景色变为 pink
    else if (canDrop) {
      backgroundColor = 'rgba(70,140,255,.1)';

    }
    // 使用 connectDropTarget 包裹住 DOM 节点，使其可以接收对应的 drag source 组件
    // connectDropTarget 包裹住的 DOM 节点才能接收 drag source 组件
    // {isActive ? 'Release to drop' : 'Drag a box here'}
    return connectDropTarget && connectDropTarget(
      <div style={{...style, backgroundColor}}>
        {
          treeNodes && treeNodes.length > 0 ? <Tree
            draggable
            onDrop={this.onDrop}
          >
            {treeNodes}
          </Tree> : <div className="placehoder">拖动数据字段至此处</div>
        }
      </div>
    );
  }
}

export default Filter;
