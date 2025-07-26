import React, {useEffect, useState, useRef} from 'react';
import {Rate, Dropdown, Menu, message} from 'antd';
import {LeftOutlined, SearchOutlined, EnvironmentOutlined} from '@ant-design/icons';
import '/public/css/element.css';
import '/public/css/shop-list.css';
import '/public/css/main.css';
import axios from "/public/js/common.js";
import {navigateTo} from "../navigateHelper.js";
import { useSearchParams } from 'react-router-dom';

const util = {
    getUrlParam: (name) => {
        const url = new URL(window.location.href);
        return url.searchParams.get(name);
    }
};

const ShopList = () => {
    const [types, setTypes] = useState([]);
    const [shops, setShops] = useState([]);
    const [typeName, setTypeName] = useState('');
    const [isReachBottom, setIsReachBottom] = useState(false);
    const [params, setParams] = useState({
        typeId: 0,
        current: 1,
        sortBy: '',
        x: 120.149993,
        y: 30.334229,
    });

    const listRef = useRef();
    const [searchParams] = useSearchParams();
    useEffect(() => {
        const typeId = Number(searchParams.get('type'));
        const name = searchParams.get('name');
        // const typeId = util.getUrlParam('type');
        // const name = util.getUrlParam('name');
        const updatedParams = {
            ...params,
            typeId,
            current: 1,
        };
        setParams(updatedParams);
        setTypeName(name);
        queryTypes();
        queryShops(updatedParams, true);
        console.log("finished!");
    }, [searchParams]);

    const queryTypes = async () => {
        try {
            const {data} = await axios.get('/shop-type/list');
            setTypes(data);
        } catch (err) {
            console.error(err);
            message.error(err.toString());
        }
    };

    const queryShops = async (customParams = params, reset = false) => {
        try {
            const {data} = await axios.get('/shop/of/type', {params: customParams});
            if (!data) return;
            const newData = data.map(s => ({...s, images: s.images.split(',')[0]}));
            if (reset){
                setShops(newData);
            }else {
                setShops(prev => [...prev, ...newData]);
            }

            console.log("shops");
        } catch (err) {
            console.error(err);
            message.error(err.toString());
        }
    };

    const handleSort = (sortBy) => {
        setParams(prev => ({...prev, sortBy, current: 1}));
        setShops([]);
        queryShops({...params, sortBy, current: 1});
    };

    const handleMenuClick = async ({key}) => {
        const selectedType = types.find(t => t.id.toString() === key);
        if (selectedType) {
            navigateTo(`/shop-list?type=${selectedType.id}&name=${selectedType.name}`)
        }
    };

    const onScroll = (e) => {
        const {scrollTop, offsetHeight, scrollHeight} = e.target;
        if (scrollTop + offsetHeight + 1 > scrollHeight && !isReachBottom) {
            setIsReachBottom(true);
            const nextPage = params.current + 1;
            setParams(prev => ({...prev, current: nextPage}));
            queryShops({...params, current: nextPage});
        } else {
            setIsReachBottom(false);
        }
    };

    const menu = (
        <Menu onClick={handleMenuClick}>
            {types.map(t => (
                <Menu.Item key={t.id}>{t.name}</Menu.Item>
            ))}
        </Menu>
    );

    return (
        <div id="app">
            <div className="header">
                <div className="header-back-btn" onClick={() => window.history.back()}><LeftOutlined/></div>
                <div className="header-title">{typeName}</div>
                <div className="header-search"><SearchOutlined/></div>
            </div>
            <div className="sort-bar">
                <div className="sort-item">
                    <Dropdown overlay={menu} trigger={["click"]}>
                        <span>{typeName} <i className="el-icon-arrow-down el-icon--right"></i></span>
                    </Dropdown>
                </div>
                <div className="sort-item" onClick={() => handleSort('')}>Distance <i
                    className="el-icon-arrow-down el-icon--right"></i></div>
                <div className="sort-item" onClick={() => handleSort('comments')}>Popularity <i
                    className="el-icon-arrow-down el-icon--right"></i></div>
                <div className="sort-item" onClick={() => handleSort('score')}>Rating <i
                    className="el-icon-arrow-down el-icon--right"></i></div>
            </div>
            <div className="shop-list" onScroll={onScroll} ref={listRef}>
                {shops.map(s => (
                    <div className="shop-box" key={s.id}
                         onClick={() => navigateTo(`/shop-detail?id=${s.id}`)}>
                        <div className="shop-img"><img src={s.images} alt=""/></div>
                        <div className="shop-info">
                            <div className="shop-title shop-item">{s.name}</div>
                            <div className="shop-rate shop-item">
                                <Rate disabled value={s.score / 10} allowHalf/> <span>{s.comments} reviews</span>
                            </div>
                            <div className="shop-area shop-item">
                                <span>{s.area}</span>
                                {s.distance && (
                                    <span>{s.distance < 1000 ? `${s.distance.toFixed(1)}m` : `${(s.distance / 1000).toFixed(1)}km`}</span>
                                )}
                            </div>
                            <div className="shop-avg shop-item">￥{s.avgPrice} / person</div>
                            <div className="shop-address shop-item">
                                <EnvironmentOutlined/> <span>{s.address}</span>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ShopList;
