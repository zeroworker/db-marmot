import React, { PureComponent } from 'react';

export default class TaskItem extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            in: false,
            STATUS_CODE: {
				STATUS_TODO: '纬度',
				STATUS_DOING: '已选纬度'
			}
        }
        this.handleDragEnter = this.handleDragEnter.bind(this);
        this.handleDragLeave = this.handleDragLeave.bind(this);
        this.handleDrop = this.handleDrop.bind(this);
    }

    async handleDragEnter(e) {
        e.preventDefault();
        if (this.props.canDragIn) {
            await this.setState({
                in: true
            })
        }
    }

    async handleDragLeave(e) {
        e.preventDefault();
        if (this.props.canDragIn) {
            await this.setState({
                in: false
            })
        }
    }

    async handleDrop(e) {
        e.preventDefault();
        this.props.dragTo(this.props.status);
        await this.setState({
            in: false
        })
    }

    render() {
        let { status, children } = this.props;
        console.log('---childred 46---', children);
        console.log('---status 47---', status);
        return (
            <div 
                id={`col-${status}`} 
                className={'col'}
                onDragEnter={this.handleDragEnter}
                onDragLeave={this.handleDragLeave}
                onDragOver={this.handleDragEnter}
                onDrop={this.handleDrop}
            >
                <header className="col-header">
                    {this.state.STATUS_CODE[status]}
                </header>
                <main className={'col-main' + (this.state.in ? ' active' : '')}>
                    {children}
                </main>
            </div>
        );
    }
}