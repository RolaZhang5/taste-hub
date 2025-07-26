import React from "react";
import { navigateTo } from "../navigateHelper.js";

const FootBar = ({ activeBtn, onChange }) => {
    const toPage = (i) => {
        onChange && onChange(i);
        const pathMap = {
            0: '/blog-edit',
            1: '/',
            2: '/map',
            3: '/chat',
            4: '/my-info'
        };
        navigateTo(pathMap[i] || '/');
    };

    return (
        <div className="foot">
            <div className={`foot-box ${activeBtn === 1 ? 'active' : ''}`} onClick={() => toPage(1)}>
                <div className="foot-view"><i className="el-icon-s-home"></i></div>
                <div className="foot-text">Home</div>
            </div>
            <div className={`foot-box ${activeBtn === 2 ? 'active' : ''}`} onClick={() => toPage(2)}>
                <div className="foot-view"><i className="el-icon-map-location"></i></div>
                <div className="foot-text">Map</div>
            </div>
            <div className="foot-box" onClick={() => toPage(0)}>
                <img className="add-btn" src="/imgs/add.png" alt=""/>
            </div>
            <div className={`foot-box ${activeBtn === 3 ? 'active' : ''}`} onClick={() => toPage(3)}>
                <div className="foot-view"><i className="el-icon-chat-dot-round"></i></div>
                <div className="foot-text">Messages</div>
            </div>
            <div className={`foot-box ${activeBtn === 4 ? 'active' : ''}`} onClick={() => toPage(4)}>
                <div className="foot-view"><i className="el-icon-user"></i></div>
                <div className="foot-text">Me</div>
            </div>
        </div>
    );
};

export default FootBar;