import React from 'react';
import { Tree } from 'antd';
import ListItem from './measureItem';
const  TreeNode = Tree.TreeNode;
import '../latitude.scss';
class MeasureL extends React.Component {
    constructor (props) {
        super(props);
        this.getTreeNode = this.getTreeNode.bind(this);
    }
    getTreeNode(treeContent) {
        if (!treeContent || treeContent.length === 0) return null;
        const treeNode = treeContent.map((value) => {
            return (
                <TreeNode
                    title={<ListItem value={value} />}     
                    key={value.key}
                >
                </TreeNode>
            )
        })
        return (
            treeNode
        )
    }
    render () {
        const treeNodes = this.getTreeNode(this.props.measureContent)
        return (
		<div className="latitude-list bor-r">
            <h3>度量</h3>
            <Tree>
                {treeNodes}
            </Tree>
        </div>
        )
    }
}
export default MeasureL