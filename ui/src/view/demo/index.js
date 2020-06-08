import React from 'react';
import { Table, Button, Message, Select, Icon } from 'antd';
const { Option } = Select;
import html2canvas from 'html2canvas';

import { DndProvider } from "react-dnd";
import Backend from "react-dnd-html5-backend";

// import { DragDropContext } from 'react-dnd';
// import HTMLBackend from 'react-dnd-html5-backend';

import Box from './components/Box';
import Dustbin from './components/Dustbin';
import Measure from './components/Measure';
import Filter from './components/Filter';

import Drilling from './components/Drilling';

import './index.scss';

import Img0101 from "../../assets/img/img_01_01.png";

export default class Intelligent extends React.Component {
	constructor(props) {
		super(props);
        this.state = {
			isShowTable: true, 
			loading: false,
			dataSource: [],
            columns: [],
			latitudeContent: [
				{
					title: '纬度0',
					key: 'name0',
				},
				{
					title: '纬度1',
					key: 'age0',
				},
				{
					title: '纬度2',
					key: 'address0',
				}
			],
			measureContent: [
				{
					title: '度量0',
					key: 'name1',
				},
				{
					title: '度量1',
					key: 'age1',
				}
			],
			drillingItems: [],//钻取/纬度
		},
		this.handleClick = this.handleClick.bind(this);
		this.handleDrilling = this.handleDrilling.bind(this);
		this.handleDeleteDrilling = this.handleDeleteDrilling.bind(this);
		this.handleDeleteDustbin = this.handleDeleteDustbin.bind(this);
		this.onChange = this.onChange.bind(this);
		this.onBlur = this.onBlur.bind(this);
		this.onFocus = this.onFocus.bind(this);
		this.onSearch = this.onSearch.bind(this);
	}

	onChange(value) {
		console.log(`selected ${value}`);
	}

	onBlur() {
		console.log('blur');
	}

	onFocus() {
		console.log('focus');
	}

	onSearch(val) {
		console.log('search:', val);
	}

	childClick = (e) => {
		console.log('-----childClick---');
		this.child1.handleMeasureCondition();
	}
	
	async handleClick() {



		// let resultList = [];
		// for (var i = 0; i < 20; i++) {
		// 	resultList.push({
		// 		key: i,
		// 		name: `伊涅斯塔 [ ${i} ]`,
		// 		age: `age [ ${i} ]`,
		// 		address: `address [ ${i} ]`,
		// 	})
		// }
		// await this.setState({ 
		// 	loading: true,
		// 	dataSource: resultList,
		// 	columns: [
		// 		{
		// 		  title: '姓名',
		// 		  dataIndex: 'name',
		// 		  key: 'name',
		// 		},
		// 		{
		// 		  title: '年龄',
		// 		  dataIndex: 'age',
		// 		  key: 'age',
		// 		},
		// 		{
		// 		  title: '住址',
		// 		  dataIndex: 'address',
		// 		  key: 'address',
		// 		},
		// 	],
		// });
		// // console.log('---73---', document.querySelector(".ant-table-wrapper"));
		// const canvasData = await html2canvas(document.querySelector(".ant-table-wrapper"));
		// // const canvasData = await html2canvas(document.querySelector("#demo"));
		// // console.log('---80---', canvasData);
		// if (!canvasData) return Message.info('生成图表失败');
		// await this.setState({
		// 	isShowTable: false,
		// });
		// document.querySelector(".container").appendChild(canvasData);
		// await this.setState({ loading: false });
	}

	async handleDrilling(data) {
		let { drillingItems } = this.state;
		let findIndex = drillingItems.findIndex((item) => item.name === data.name);
		if (findIndex >= 0) return;
		drillingItems.push(data);
		await this.setState({drillingItems});
	}

	async handleDeleteDrilling(index) {
		let drillingItems = this.state.drillingItems;
		drillingItems.splice(index, 1);
		await this.setState({ drillingItems });
	}

	async handleDeleteDustbin(index) {
		console.log(index);
	}

    render() {
		let { isShowTable, drillingItems } = this.state;
        return (
            <div style={{height:"100%"}}>
				<header className="header-cont fx fx-x-between fx-y-center">
					<div className="input-cont fx fx-y-center"><i className="icon-dashboard m-r-10"></i><input type="text" name="" id="" value="" placeholder="请输入项目名称"/></div>
					<div className="fx fx-y-center btn-cont">
						<div className="btn-grp fx">
							<div className="btn on btn-edit">预览</div>
							<div className="btn btn-preview">生成</div>
						</div>
						<div className="btn btn-save">保存</div>
					</div>
				</header>
				<article className="fx main">
					<div className="left-cont">
						<div className="main-icons-grp fx">
							<div className="icon-list fn-hide">
								<ul className="fx">
									<li>
										<div><i className="icon-search-style-01"></i></div>
										<div><i className="icon-search-style-02"></i></div>
										<div><i className="icon-search-style-03"></i></div>
									</li>
									<li><i className="icon-text"></i></li>
									<li><i className="icon-link"></i></li>
									<li><i className="icon-tab"></i></li>
									<li><i className="icon-img"></i></li>
								</ul>
							</div>
							<div className="chart-list fn-hide">
								<ul className="fx">
									<li className="has-after">
										<div className="icon-chart-img first-list-1"></div>
										<div className="icon-chart-img first-list-2"></div>
										<div className="icon-chart-img first-list-3"></div>
										<div className="icon-chart-img first-list-4"></div>
									</li>
									<li className="has-after">
										<div className="icon-chart-img second-list-1"></div>
										<div className="icon-chart-img second-list-2"></div>
										<div className="icon-chart-img second-list-3"></div>
										<div className="icon-chart-img second-list-4"></div>
										<div className="icon-chart-img second-list-5"></div>
										<div className="icon-chart-img second-list-6"></div>
										<div className="icon-chart-img second-list-7"></div>
									</li>
									<li>
										<div className="icon-chart-img third-list-1"></div>
									</li>
									<li>
										<div className="icon-chart-img fourth-list-1"></div>
									</li>
									<li>
										<div className="icon-chart-img fifth-list-1"></div>
									</li>
									<li className="has-after">
										<div className="icon-chart-img sixth-list-1"></div>
										<div className="icon-chart-img sixth-list-2"></div>
										<div className="icon-chart-img sixth-list-3"></div>
										<div className="icon-chart-img sixth-list-4"></div>
									</li>
									<li className="has-after">
										<div className="icon-chart-img seventh-list-1"></div>
										<div className="icon-chart-img seventh-list-2"></div>
									</li>
									<li className="has-after">
										<div className="icon-chart-img eighth-list-1"></div>
										<div className="icon-chart-img eighth-list-2"></div>
									</li>
									<li>
										<div className="icon-chart-img ninth-list-1"></div>
									</li>
									<li className="has-after">
										<div className="icon-chart-img tenth-list-1"></div>
										<div className="icon-chart-img tenth-list-2"></div>
									</li>
									<li className="has-after">
										<div className="icon-chart-img eleventh-list-1"></div>
										<div className="icon-chart-img eleventh-list-2"></div>
									</li>
									<li className="has-after">
										<div className="icon-chart-img twelfth-list-1"></div>
										<div className="icon-chart-img twelfth-list-2"></div>
									</li>
									<li>
										<div className="icon-chart-img thirteenth-list-1"></div>
									</li>
									<li className="has-after">
										<div className="icon-chart-img fourteenth-list-1"></div>
										<div className="icon-chart-img fourteenth-list-2"></div>
									</li>
									<li>
										<div className="icon-chart-img fifteenth-list-1"></div>
									</li>
									<li>
										<div className="icon-chart-img sixteenth-list-1"></div>
									</li>
								</ul>
							</div>
						</div>
						<div className="container">
							<section className={`chart-unit ${isShowTable ? '' : 'fn-hide'}`}>
								<Table
									dataSource={this.state.dataSource} 
									columns={this.state.columns} 
									pagination={false} 
									bordered
								/>
							</section>
						</div>
					</div>
					<div className="right-cont fx-shrink">
						<div className="fx fx-x-between fx-y-center title-cont">
							<h2>标题</h2>
							<div className="change-chart-type">更改图表类型</div>
						</div>
						<div className="chart-type-list fn-hide">
							<div>
								<h3>线图</h3>
								<ul className="fx fx-wrap">
									<li className="on"><img src={Img0101}/></li>
									<li><img src="../assets/img/img_01_02.png"/></li>
									<li><img src="../assets/img/img_01_03.png"/></li>
									<li><img src="../assets/img/img_01_04.png"/></li>
								</ul>
							</div>
							<div>
								<h3>柱图</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_02_01.png"/></li>
									<li><img src="../assets/img/img_02_02.png"/></li>
									<li><img src="../assets/img/img_02_03.png"/></li>
									<li><img src="../assets/img/img_02_04.png"/></li>
									<li><img src="../assets/img/img_02_05.png"/></li>
									<li><img src="../assets/img/img_02_06.png"/></li>
									<li><img src="../assets/img/img_02_07.png"/></li>
								</ul>
							</div>
							<div>
								<h3>组合图</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_03_01.png"/></li>
								</ul>
							</div>
							<div>
								<h3>饼图</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_04_01.png"/></li>
								</ul>
							</div>
							<div>
								<h3>交叉表</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_05_01.png"/></li>
								</ul>
							</div>
							<div>
								<h3>区域色彩地图</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_06_01.png"/></li>
									<li><img src="../assets/img/img_06_02.png"/></li>
									<li><img src="../assets/img/img_06_03.png"/></li>
									<li><img src="../assets/img/img_06_04.png"/></li>
								</ul>
							</div>
							<div>
								<h3>指标看板</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_07_01.png"/></li>
									<li><img src="../assets/img/img_07_02.png"/></li>
								</ul>
							</div>
							<div>
								<h3>仪表盘</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_08_01.png"/></li>
									<li><img src="../assets/img/img_08_02.png"/></li>
								</ul>
							</div>
							<div>
								<h3>分面散点图</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_09_01.png"/></li>
								</ul>
							</div>
							<div>
								<h3>漏斗图</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_10_01.png"/></li>
									<li><img src="../assets/img/img_10_02.png"/></li>
								</ul>
							</div>
							<div>
								<h3>雷达图</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_11_01.png"/></li>
									<li><img src="../assets/img/img_11_02.png"/></li>
								</ul>
							</div>
							<div>
								<h3>矩形树图</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_12_01.png"/></li>
									<li><img src="../assets/img/img_12_02.png"/></li>
								</ul>
							</div>
							<div>
								<h3>词云图</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_13_01.png"/></li>
								</ul>
							</div>
							<div>
								<h3>桑基图</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_14_01.png"/></li>
									<li><img src="../assets/img/img_14_02.png"/></li>
								</ul>
							</div>
							<div>
								<h3>排行榜</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_15_01.png"/></li>
								</ul>
							</div>
							<div>
								<h3>翻牌器</h3>
								<ul className="fx fx-wrap">
									<li><img src="../assets/img/img_16_01.png"/></li>
								</ul>
							</div>
						</div>
						<div className="tabs-list">
							<ul className="fx fx-y-center">
								<li className="on">tab1</li>
								<li>tab2</li>
								<li>tab3</li>
							</ul>
						</div>
						<div>

						<div>
							<DndProvider backend={Backend}>
								<div className="fx source-grp">
									<div className="source-list bor-r">
										<Select
											showSearch
											style={{ width: 180 }}
											placeholder="请选择数据集"
											optionFilterProp="children"
											onChange={this.onChange}
											onFocus={this.onFocus}
											onBlur={this.onBlur}
											onSearch={this.onSearch}
											filterOption={(input, option) =>
												option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
											}
										>
											<Option value="jack">Jack</Option>
											<Option value="lucy">Lucy</Option>
											<Option value="tom">Tom</Option>
										</Select>
										<div className="bor-b">
											<h3>维度</h3>
											<div className="option-list">
												<Box name="Glass" type="LATITUDE" orderBy="ASC" dataType="date"/>
												<Box name="Banana" type="LATITUDE" orderBy="ASC" dataType="date"/>
												<Box name="Paper" type="LATITUDE" orderBy="ASC" dataType="string"/>
												<Box name="aa" type="LATITUDE" orderBy="ASC" dataType="string"/>
												<Box name="ss" type="LATITUDE" orderBy="ASC" dataType="string"/>
												<Box name="dd" type="LATITUDE" orderBy="ASC" dataType="string"/>
												<Box name="ff" type="LATITUDE" orderBy="ASC" dataType="string"/>
												<Box name="cc" type="LATITUDE" orderBy="ASC" dataType="string"/>
												<Box name="hh" type="LATITUDE" orderBy="ASC" dataType="string"/>
												<Box name="jj" type="LATITUDE" orderBy="ASC" dataType="string"/>
												<Box name="kk" type="LATITUDE" orderBy="ASC" dataType="string"/>
												<Box name="ll" type="LATITUDE" orderBy="ASC" dataType="string"/>
											</div>
										</div>
										<div>
											<h3>度量 <Icon type="mobile" /></h3>
											<Box name="demo" type="MEASURE" orderBy="SUM" orderLable="求和" dataType="number"/>
											<Box name="Paper1" type="MEASURE" orderBy="SUM" orderLable="求和" dataType="number"/>
											<Box name="Banana1" type="MEASURE" orderBy="SUM" orderLable="求和" dataType="number"/>
										</div>
									</div>
									<div className="target-list">
										{
											this.state.drillingItems.length > 0 
											? 
											<div>
												<h3>钻取/维度</h3>
												<Drilling 
													drillingItems={this.state.drillingItems} 
													handleDeleteDrilling={this.handleDeleteDrilling}/>
											</div> 
											: 
											null
										}
										<h3>数据列</h3>
										<Measure 
											name="measure" 
											onRef={(ref)=>{ this.child1 = ref }}
										/>

										<h3 className="m-t-15">过滤器</h3>
										<Filter 
											name="filter" 
											onRef={(ref)=>{ this.child2 = ref }}
										/>

										<div className="btn-grp">
											<div className="tab-btn">
												<span className="on">聚合</span>
												<span>明细</span>
											</div>
											<Button
												className="btn-update" 
												type="primary" 
												onClick={this.childClick} 
												loading={this.state.loading}>
													更新
											</Button>
										</div>
									</div>

								</div>
							</DndProvider>
						</div>
						
						</div>
					</div>
				</article>
			</div>
        )
    }
}