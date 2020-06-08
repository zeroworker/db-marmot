import React from 'react';
import { Tree } from 'antd';
import { DragSource } from 'react-dnd';
import PropTypes from 'prop-types';
const  TreeNode = Tree.TreeNode;

const dragName = 'measure';

const spec = {
    beginDrag(props) {
        return { ...props.value }
    }
}
function collect (connect, monitor) {
    return {
        connectDragSource: connect.dragSource(),
        isDragging: monitor.isDragging()
    }
}
class MeasureItem extends React.Component {
    constructor (props) {
        super(props);
    }
    render () {
        const { connectDragSource, isDragging, value } = this.props;
        return connectDragSource(
            <div style={{
                float: "left",
                opacity: isDragging ? 0.5 : 1,
                cursor: isDragging ? 'move': '',
            }}>
                <div>{value.title}</div>
            </div>
        )
    }
}
MeasureItem.propTypes = {
    connectDragSource: PropTypes.func.isRequired,
    isDragging: PropTypes.bool.isRequired,
}
export default DragSource(dragName, spec, collect)(MeasureItem)