import React from 'react';
import {Icon, Menu, Tree} from 'antd';
import PropTypes from 'prop-types';

import {DropTarget} from 'react-dnd';
import ItemTypes from '../types';

import Order from './Order';

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
class Measure extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      treeContent: [],
    }
    this.handleClickMenu = this.handleClickMenu.bind(this);
    this.onDrop = this.onDrop.bind(this);
    this.loop = this.loop.bind(this);
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

  async handleDeleteMeasure(index) {
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

  // 由数组生成Tree
  getTreeNode(treeContent) {
    if (!treeContent || treeContent.length === 0) return null;
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
                      {value.name}({value.orderLable}）
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
                        <Icon type={value.orderBy === 'ASC' ? 'sort-ascending' : 'sort-descending'}/>
                        {value.name}
                      </div>
                }
              </div>
              <div className="fx fx-x-between fx-y-center operation-cont">
                <Order
                  index={index}
                  type={value.type}
                  handleClickMenu={this.handleClickMenu}
                />
                <Icon type="delete" className="m-l-5" onClick={() => this.handleDeleteMeasure(index)}/>
              </div>
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

export default Measure;
