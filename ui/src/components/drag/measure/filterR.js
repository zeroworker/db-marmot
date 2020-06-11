import React from 'react';
import {Tree} from 'antd';
import {DropTarget} from 'react-dnd';
import '../latitude.scss';

const TreeNode = Tree.TreeNode;

class FilterR extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedKey: '',
      treeContent: []
    }
    this.loop = this.loop.bind(this);
    this.onDrop = this.onDrop.bind(this);
    this.onNodeSelect = this.onNodeSelect.bind(this);
    this.getTreeNode = this.getTreeNode.bind(this);
    this.setItemKeys = this.setItemKeys.bind(this);
    this.addItem = this.addItem.bind(this);
  }

//   onDragEnter = (info) => {
//     console.log('---15---', info);
//   }
  // 遍历数组,找出key值与之相同的对象(节点)，执行callback函数
  // 若key值不匹配且含有children，则循环遍历
  // 否则不执行任何操作
  loop(data, key, callback) {
    data.forEach((item, index, arr) => {
      if (item.key === key) return callback(item, index, arr);
      if (item.children) {
        return this.loop(item.children, key, callback);
      }
    })
  }

  onDrop(info) {
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

  onNodeSelect(selectedKeys, e) {
    console.log('select', selectedKeys, e);
    this.setState({selectedKey: selectedKeys[0]});
  }

  // 由数组生成Tree
  getTreeNode(treeContent) {
    if (!treeContent || treeContent.length === 0) {
      return null
    }
    const treeNode = treeContent.map((value) => {
      return (
        <TreeNode
          draggable
          title={<span>{value.title}</span>}
          key={value.key}
        >
          {this.getTreeNode(value.children)}
        </TreeNode>
      )
    })
    return (
      treeNode
    )
  }

  setItemKeys(Item) {
    const itemIndex = this.state.treeContent.findIndex((value, index, arr) => {
      return value.key === Item.key;
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

  // 新增Item
  addItem(Item) {
    if (Item.children && Item.children.length > 0) return;
    // 为新加Item设置新的key
    let {treeContent} = this.state
    Item = this.setItemKeys(Item)
    if (!Item) {
      this.setState({treeContent});
      return;
    }
    console.log('new key:', Item.key)
    treeContent.push(Item)
    this.setState({treeContent})
  }

  render() {
    const {treeContent} = this.state;
    const treeNodes = this.getTreeNode(treeContent);
    const {connectDropTarget} = this.props;
    return connectDropTarget(
      <div className="latitude-list">
        <h3>过滤器</h3>
        <Tree
          draggable
          onDragEnter={this.onDragEnter}
          onDrop={this.onDrop}
          onSelect={this.onNodeSelect}
        >
          {treeNodes}
        </Tree>
      </div>
    )
  }
}

const spec = {
  // monitor.getItem()可获取之前dragsource在beginDrag中return的Object
  //component可直接调用组件内部方法
  drop(props, monitor, component) {
    component.addItem(monitor.getItem())
  }
}

function collect(connect, monitor) {
  return {
    connectDropTarget: connect.dropTarget(),
    isOver: monitor.isOver(),
  }
}

export default DropTarget('measure', spec, collect)(FilterR)
