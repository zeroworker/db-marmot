import React from 'react';
import {BrowserRouter, Route, Switch} from 'react-router-dom';

import IntelligentPage from "./intelligent";

import TestPages from "./view/demo/index";

const BasicRoute = () => (
  <BrowserRouter>
    <Switch>
      <Route exact path="/" component={IntelligentPage}/>
      <Route exact path="/demo" component={TestPages}/>
    </Switch>
  </BrowserRouter>
);


export default BasicRoute;
