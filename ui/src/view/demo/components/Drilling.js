import React from 'react';
import { Tree, Icon } from 'antd';
const  TreeNode = Tree.TreeNode;

export default class Drilling extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            show: true
        }
    }

    handleDeleteDrilling(index) {
        console.log('-----75------', index);
        this.props.handleDeleteDrilling(index);
	}

    getTreeNode(data) {
        if (!data || data.length === 0) return null;
        const treeNode = data.map((value, index) => {
            return (
                <TreeNode
					title={
						<div className="fx fx-x-between fx-y-center">
							{value.name}
                            <Icon type="delete" onClick={()=>this.handleDeleteDrilling(index)}/>
						</div>
					}
					key={value.name}
				>
				</TreeNode>
            )
        })
        return (
            treeNode
        )
    }

    render() {
        console.log('---drillingItems---', this.props.drillingItems);
        let { drillingItems } = this.props;
        const treeNodes = this.getTreeNode(drillingItems);
        return (
            <Tree>
                {treeNodes}
            </Tree>
        )
    }
}