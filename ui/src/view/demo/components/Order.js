import React from 'react';
import {Dropdown, Icon, Menu} from 'antd';

const {SubMenu} = Menu;

class Order extends React.Component {
  constructor(props) {
    super(props);
    this.state = {}
    this.getMenu = this.getMenu.bind(this);
  }

  async handleClickMenu(value, title) {
    this.props.handleClickMenu(value, title, this.props.index);
  }

  getMenu(type, index) {
    if (type === 'LATITUDE') {
      return (
        <Menu>
          <Menu.Item key="ASC" onClick={() => this.handleClickMenu('ASC', '升序', index)}>升序</Menu.Item>
          <Menu.Item key="DESC" onClick={() => this.handleClickMenu('DESC', '降序', index)}>降序</Menu.Item>
        </Menu>
      )
    } else {
      return (
        <Menu>
          <Menu.Item key="SUM" onClick={() => this.handleClickMenu('SUM', '求和', index)}>求和</Menu.Item>
          <Menu.Item key="AVG" onClick={() => this.handleClickMenu('AVG', '平均值', index)}>平均值</Menu.Item>
          <Menu.Item key="COUNT" onClick={() => this.handleClickMenu('COUNT', '计数', index)}>计数</Menu.Item>
          <Menu.Item key="DECOUNT" onClick={() => this.handleClickMenu('DECOUNT', '去重计数', index)}>去重计数</Menu.Item>
          <Menu.Item key="NONEPOLY" disabled>无聚合</Menu.Item>
          <SubMenu
            key="sub1"
            title={
              <span>更多聚合方式</span>
            }
          >
            <Menu.ItemGroup>
              <Menu.Item key="MAX" onClick={() => this.handleClickMenu('MAX', '最大值', index)}>最大值</Menu.Item>
              <Menu.Item key="MIN" onClick={() => this.handleClickMenu('MIN', '最小值', index)}>最小值</Menu.Item>
              <Menu.Item key="ZTBZC" onClick={() => this.handleClickMenu('ZTBZC', '总体标准差', index)}>总体标准差</Menu.Item>
              <Menu.Item key="YBBZC" onClick={() => this.handleClickMenu('YBBZC', '样本标准差', index)}>样本标准差</Menu.Item>
              <Menu.Item key="YBBZC" onClick={() => this.handleClickMenu('YBBZC', '总体方差', index)}>总体方差</Menu.Item>
              <Menu.Item key="YBFC" onClick={() => this.handleClickMenu('YBFC', '样本方差', index)}>样本方差</Menu.Item>
            </Menu.ItemGroup>
          </SubMenu>
        </Menu>
      )
    }
  }

  render() {
    const {type, index} = this.props;
    return (
      <Dropdown overlay={this.getMenu(type, index)} trigger={['click']} key={index}>
        <a className="ant-dropdown-link" href="#"><Icon type="down"/></a>
      </Dropdown>
    )
  }
}

export default Order;
