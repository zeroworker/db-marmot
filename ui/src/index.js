import React, { PureComponent } from 'react';
import { render } from 'react-dom';
import Router from './router';
import 'antd/dist/antd.css'; // or 'antd/dist/antd.less'
require('./base.scss');
require("./common.scss");
require("./override.css");
render(
    <Router/>,
    document.getElementById('app')
);