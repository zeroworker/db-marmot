import React, { PureComponent } from 'react';

import './style.css';

export default class TaskItem extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
        }
        this.handleDragStart = this.handleDragStart.bind(this);
    }

    async handleDragStart(e) {
        console.log('---14  do---');
        this.props.onDragStart(this.props.id);
    }

    render() {
        let { id, username, active, onDragEnd } = this.props;
        return (
            <div 
                onDragStart={this.handleDragStart}
                onDragEnd={onDragEnd}
                id={`item-${id}`} 
                className={'item' + (active ? ' active' : '')}
                draggable="true"
            >
                <header className="item-header">
                    <span className="item-header-username">{username}<a>点我</a></span>
                </header>
            </div>
        );
    }
    
}