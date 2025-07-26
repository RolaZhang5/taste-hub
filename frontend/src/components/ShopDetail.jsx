// import {navigateTo} from '../../src/navigateHelper'
import React, {useState, useEffect} from 'react';
import axios from "/public/js/common.js";
import {useSearchParams} from 'react-router-dom';
import {Rate, message} from 'antd'; // Example uses Ant Design
import {LeftOutlined} from '@ant-design/icons';
import '/public/css/element.css';
import styles from '/public/css/shop-detail.module.css'

// import '/public/css/main.css';
function ShopDetail() {
    const [searchParams] = useSearchParams();
    const shopId = searchParams.get('id');

    const [shop, setShop] = useState({});
    const [vouchers, setVouchers] = useState([]);

    useEffect(() => {
        if (shopId) {
            fetchShop(shopId);
            fetchVouchers(shopId);
        }
    }, [shopId]);

    const goBack = () => {
        window.history.back();
    };

    const fetchShop = async (id) => {
        try {
            const {data} = await axios.get(`/shop/${id}`);
            setShop({
                ...data,
                images: data.images.split(','),
            });
        } catch (err) {
            message.error(err.toString());
        }
    };

    const fetchVouchers = async (id) => {
        try {
            const {data} = await axios.get(`/voucher/list/${id}`);
            setVouchers(data);
        } catch (err) {
            message.error(err.toString());
        }
    };

    const formatTime = (v) => {
        const b = new Date(v.beginTime);
        const e = new Date(v.endTime);
        return `${b.getMonth() + 1}月${b.getDate()}日 ${b.getHours()}:${formatMinutes(b.getMinutes())} ~ ${e.getHours()}:${formatMinutes(e.getMinutes())}`;
    };

    const formatMinutes = (m) => (m < 10 ? `0${m}` : m);

    const isNotBegin = (v) => new Date(v.beginTime).getTime() > Date.now();
    const isEnd = (v) => new Date(v.endTime).getTime() < Date.now();

    const handleSeckill = async (v) => {
        if (!localStorage.getItem('token')) {
            message.error('Please log in first');
            setTimeout(() => window.location.href = '/login.html', 200);
            return;
        }
        if (isNotBegin(v)) {
            message.error('Coupon sale has not started yet!');
            return;
        }
        if (isEnd(v)) {
            message.error('Coupon sale has ended!');
            return;
        }
        if (v.stock < 1) {
            message.error('Out of stock, please refresh and try again!');
            return;
        }
        try {
            const {data} = await axios.post(`/voucher-order/seckill/${v.id}`);
            message.success('Purchase successful, order id: ' + data);
        } catch (err) {
            message.error(err.toString());
        }
    };

    return (
        <div id="app" style={{background: '#fff'}}>
            <div className="header">
                <button className={styles["header-back-btn"]} onClick={goBack}><LeftOutlined/></button>
                {/*<div className={styles["header-title"]}>{shop.name}</div>*/}
                <div className={styles["header-share"]}>...</div>
            </div>

            <div className={styles["shop-info-box"]}>
                <h1 className={styles["shop-title"]}>{shop.name}</h1>
                <div className={styles["shop-rate"]}>
                    <div>
                        <Rate disabled allowHalf value={(shop.score || 0) / 10}/>
                        <span style={{color: 'red', marginLeft: '10px'}}>{((shop.score || 0) / 10).toFixed(1)}</span>
                    </div>
                    {/*<span style={{ textAlign: 'left', color: 'red' }}>{((shop.score || 0) / 10).toFixed(1)}</span>*/}
                    <span>{shop.comments} reviews</span>
                </div>
                <div
                    // className={styles["shop-rate-info"]}>Taste:{shop.taste} Environment:{shop.environment} Service:{shop.service}</div>
                    className={styles["shop-rate-info"]}>Taste: 4.9 Environment: 4.8 Service: 4.7
                </div>
                <div className={styles["shop-images"]}>
                    {(shop.images || []).map((s, i) => (
                        <img key={i} src={s} alt=""/>
                    ))}
                </div>
                <div className={styles["shop-address"]}>
                    <i className="el-icon-map-location"/>
                    <span>{shop.address}</span>
                </div>
            </div>

            <div className={styles["shop-divider"]}></div>

            <div className={styles["shop-open-time"]}>
                <span><i className="el-icon-watch"></i></span>
                <div>Opening Hours</div>
                <div>{shop.openHours}</div>
                <span className={styles["line-right"]}>
          View Details <i className="el-icon-arrow-right"></i>
        </span>
            </div>

            <div className={styles["shop-divider"]}></div>

            <div className={styles["shop-voucher"]}>
                <div>
                    <span className={styles["voucher-icon"]}>Coupon</span>
                    <span style={{fontWeight: 'bold', marginLeft: '5px'}}>Voucher</span>
                </div>
                {vouchers.filter(v => !isEnd(v)).map(v => (
                    <div key={v.id} className={styles["voucher-box"]}>
                        <div className={styles["voucher-circle"]}>
                            <div className={styles["voucher-b"]}></div>
                            <div className={styles["voucher-b"]}></div>
                            <div className={styles["voucher-b"]}></div>
                        </div>
                        <div className={styles["voucher-left"]}>
                            <div className={styles["voucher-title"]}>{v.title}</div>
                            <div className={styles["voucher-subtitle"]}>{v.subTitle}</div>
                            <div className={styles["voucher-price"]}>￥{v.payValue}
                                <span>{(v.payValue * 10) / v.actualValue} off</span></div>
                        </div>
                        <div className={styles["voucher-right"]}>
                            {v.type ? (
                                <div className={styles["seckill-box"]}>
                                    <button
                                        className={styles[`voucher-btn ${isNotBegin(v) || v.stock < 1 ? 'disable-btn' : ''}`]}
                                        onClick={() => handleSeckill(v)}
                                    >
                                        Limited Time Sale
                                    </button>
                                    <div className={styles["seckill-stock"]}>Remaining {v.stock}</div>
                                    <div className={styles["seckill-time"]}>{formatTime(v)}</div>
                                </div>
                            ) : (
                                <button className={styles["voucher-btn"]} onClick={() => handleSeckill(v)}>Buy Now</button>
                            )}
                        </div>
                    </div>
                ))}
            </div>
            <div className={styles["shop-divider"]}></div>
            {/* Comments section can be added here */}
            <div className={styles["shop-comments"]}>
                <div className={styles["comments-head"]}>
                    <div>User Reviews <span>（119）</span></div>
                    <div><i className="el-icon-arrow-right"></i></div>
                </div>
                <div className={styles["comment-tags"]}>
                    <div className="tag">Great Taste (19)</div>
                    <div className="tag">Beef Lover (16)</div>
                    <div className="tag">Good Dishes (11)</div>
                    <div className="tag">Repeat Customer (4)</div>
                    <div className="tag">Generous Portions (4)</div>
                    <div className="tag">Easy Parking (3)</div>
                    <div className="tag">Seafood Excellent (3)</div>
                    <div className="tag">Great Drinks (3)</div>
                    <div className="tag">Friends Gathering (6)</div>
                </div>
                <div className={styles["comment-list"]}>
                    {[1, 2, 3].map((i) => (
                        <div className={styles["comment-box"]} key={i}>
                            <div className={styles["comment-icon"]}>
                                <img
                                    src="https://p0.meituan.net/userheadpicbackend/57e44d6eba01aad0d8d711788f30a126549507.jpg%4048w_48h_1e_1c_1l%7Cwatermark%3D0"
                                    alt=""/>
                            </div>
                            <div className={styles["comment-info"]}>
                                <div className={styles["comment-user"]}>Ye Xiaoyi <span>Lv5</span></div>
                                <div style={{display: 'flex'}}>
                                    Rating
                                    <Rate style={{fontSize: '14px', marginLeft: '5px'}} disabled value={4.5}></Rate>
                                </div>
                                <div style={{padding: '5px 0' ,fontSize: '14px'}}>
                                    Bought a coupon on a platform, price is good for work meals, although cheap, this place is not...
                                </div>
                                <div className={styles["comment-images"]}>
                                    <img
                                        src="https://qcloud.dpfile.com/pc/6T7MfXzx7USPIkSy7jzm40qZSmlHUF2jd-FZUL6WpjE9byagjLlrseWxnl1LcbuSGybIjx5eX6WNgCPvcASYAw.jpg"
                                        alt=""/>
                                    <img
                                        src="https://qcloud.dpfile.com/pc/sZ5q-zgglv4VXEWU71xCFjnLM_jUHq-ylq0GKivtrz3JksWQ1f7oBWZsxm1DWgcaGybIjx5eX6WNgCPvcASYAw.jpg"
                                        alt=""/>
                                    <img
                                        src="https://qcloud.dpfile.com/pc/xZy6W4NwuRFchlOi43DVLPFsx7KWWvPqifE1JTe_jreqdsBYA9CFkeSm2ZlF0OVmGybIjx5eX6WNgCPvcASYAw.jpg"
                                        alt=""/>
                                    <img
                                        src="https://qcloud.dpfile.com/pc/xZy6W4NwuRFchlOi43DVLPFsx7KWWvPqifE1JTe_jreqdsBYA9CFkeSm2ZlF0OVmGybIjx5eX6WNgCPvcASYAw.jpg"
                                        alt=""/>
                                </div>
                                <div>
                                    Views 641 &nbsp;&nbsp;&nbsp;&nbsp; Comments 5
                                </div>
                            </div>
                        </div>
                    ))}

                    <div
                        style={{display: 'flex', justifyContent: 'space-between', padding: '15px 0', borderTop: '1px solid #f1f1f1', marginTop: '10px'}}>
                        <div>View all 119 reviews</div>
                        <div><i className="el-icon-arrow-right"></i></div>
                    </div>
                </div>
                <div className={styles["shop-divider"]}></div>
                <div className={styles["copyright"]}>
                    copyright ©2021 hmdp.com
                </div>
            </div>
        </div>
    );
}

export default ShopDetail;
