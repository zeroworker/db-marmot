import React, { PureComponent } from 'react';
import HTML5Backend from 'react-dnd-html5-backend';
import { DndProvider } from 'react-dnd';

import Tz from './tz.jsx';

class TzIndex extends PureComponent {
    constructor(props) {
        super(props);
    }
    render () {
        return (
            <DndProvider backend={HTML5Backend}>
                <Tz />
            </DndProvider>
        )
    }
}
export default TzIndex;