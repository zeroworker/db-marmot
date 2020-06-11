import React from 'react';
import HTML5Backend from 'react-dnd-html5-backend';
import {DndProvider} from 'react-dnd';
import LatitudeL from './latitude/latitudeL';
import LatitudeR from './latitude/latitudeR';
import './latitude.scss';

class DragIndex extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    // <MeasureL measureContent={this.props.measureContent}/>
    return (
      <DndProvider backend={HTML5Backend}>

        <div className="fx latitude-grp">
          <LatitudeL latitudeContent={this.props.latitudeContent}/>
          <LatitudeR/>
        </div>

      </DndProvider>
    )
  }
}

export default DragIndex;
