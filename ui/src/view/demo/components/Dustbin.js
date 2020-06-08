import React from 'react';
import { Tree, Icon, Menu, Dropdown } from 'antd';
const { SubMenu } = Menu;
const  TreeNode = Tree.TreeNode;
import PropTypes from 'prop-types';
import { DropTarget } from 'react-dnd';
import ItemTypes from '../types';

import Order from './Order';

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
	drop (props, monitor, component) {
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
class Dustbin extends React.Component {

	constructor (props) {
        super(props);
        this.state = {
            treeContent: []
		}
		this.handleClickMenu = this.handleClickMenu.bind(this);
    }

    static propTypes = {
        canDrop: PropTypes.bool.isRequired,
        isOver: PropTypes.bool.isRequired,
        connectDropTarget: PropTypes.func.isRequired
	}

	componentDidMount() {
        this.props.onRef(this);
    }

	handleCondition() {
		console.log('子组件的方法被父组件调用');
		console.log('---treeContent---', this.state.treeContent);
    }

	// 新增Item
	addItem(Item) {
		// 为新加Item设置新的key
		let { treeContent } = this.state;
		Item = this.setItemKeys(Item);
		if (!Item) return this.setState({ treeContent });
		treeContent.push(Item);
		this.setState({ treeContent });
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
	
	async handleDeleteDustbin(index) {
		console.log('-----75------', index);
		// this.props.handleDeleteDustbin(index);
		let treeContent = this.state.treeContent;
		treeContent.splice(index, 1);
		await this.setState({ treeContent });
	}
	
	handleDrilling(data) {
		console.log('---钻取---', data);
		this.props.handleDrilling(data);
	}

	async handleClickMenu(data, title, index) {
		let treeContent = this.state.treeContent;
		treeContent[index].flag = title;
		await this.setState({ treeContent });
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
									value.type === 'MEASURE' ? <i className="icon-no"></i> : <i className="icon-str"></i>
								}
								{
									value.flag ? value.name + ' (' + value.flag + '）' : value.name
								}
							</div>
							<div className="fx fx-x-between fx-y-center operation-cont">
								<Order
									index={index} 
									type={value.type} 
									handleClickMenu={this.handleClickMenu}
									/>
								<Icon className="m-l-5 m-r-5" type="block" onClick={()=>this.handleDrilling(value)}/>
								<Icon type="delete" onClick={()=>this.handleDeleteDustbin(index)}/>
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
		const { treeContent } = this.state;
		const treeNodes = this.getTreeNode(treeContent);
		const { canDrop, isOver, connectDropTarget } = this.props;
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
			<div style={{ ...style, backgroundColor }}>
				{
					treeNodes && treeNodes.length > 0 
					? <Tree
                    draggable
                >
                    {treeNodes}
                </Tree> : <div className="placehoder">拖动数据字段至此处</div>
				}
			</div>
		);
	}
}

export default Dustbin;