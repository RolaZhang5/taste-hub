import React, {useEffect, useState} from "react";
import axios from "/public/js/common.js";
import "/public/css/element.css";
import "/public/css/blog-detail.css";
import "/public/css/main.css";
import {Button, message} from "antd";
import ImageSlider from "./ImageSlider.jsx";
import {navigateTo} from "../navigateHelper.js";
import { LikeOutlined, LikeFilled } from '@ant-design/icons';

const BlogDetail = () => {
    const [blog, setBlog] = useState({images: []});
    const [shop, setShop] = useState({});
    const [likes, setLikes] = useState([]);
    const [user, setUser] = useState({});
    const [followed, setFollowed] = useState(false);

    const queryParam = (key) => {
        const params = new URLSearchParams(window.location.search);
        return params.get(key);
    };

    const queryBlogById = async (id) => {
        try {
            const {data} = await axios.get(`/blog/${id}`);
            data.images = data.images.split(",");
            setBlog(data);
            queryShopById(data.shopId);
            queryLikeList(id);
            queryLoginUser(data.userId);
        } catch (e) {
            message.error("Failed to get blog, " + e.message);
        }
    };

    const queryShopById = async (shopId) => {
        try {
            const {data} = await axios.get(`/shop/${shopId}`);
            data.image = data.images.split(",")[0];
            setShop(data);
        } catch (e) {
            message.error("Failed to get shop info! " + e.message);
        }
    };

    const queryLikeList = async (id) => {
        const {data} = await axios.get(`/blog/likes/${id}`);
        setLikes(data);
    };

    const queryLoginUser = async (blogUserId) => {
        try {
            const {data} = await axios.get("/user/me");
            setUser(data);
            if (data.id !== blogUserId) {
                const res = await axios.get(`/follow/or/not/${blogUserId}`);
                setFollowed(res.data);
            }
        } catch (e) {
            message.error("Not logged in! " + e.message);
        }
    };

    const follow = async () => {
        await axios.put(`/follow/${blog.userId}/${!followed}`);
        setFollowed(!followed);
        message.success(followed ? "Unfollowed" : "Followed");
    };

    const addLike = async () => {
        await axios.put(`/blog/like/${blog.id}`);
        queryBlogById(blog.id);
    };

    const formatDate = (time) => {
        const date = new Date(time);
        return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
    };

    const goBack = () => {
        window.history.back();
    };

    const toOtherInfo = () => {
        if (blog.userId === user.id) {
            navigateTo('/my-info');
        } else {
            window.location.href = `/other-info.html?id=${blog.userId}`;
        }
    };

    useEffect(() => {
        const id = queryParam("id");
        queryBlogById(id);
    }, []);

    return (
        <div id="app" style={{background: '#fff'}}>
            <div className="header">
                <div className="header-back-btn" onClick={goBack}>
                    <i className="el-icon-arrow-left"></i>
                </div>
                <div className="header-title"></div>
                <div className="header-share">...</div>
            </div>

            <div style={{height: "85vh", overflowY: "scroll", overflowX: "hidden"}}>
                <div className="blog-info-box" style={{overflow: "hidden"}}>
                    <ImageSlider images={blog.images} />
                </div>

                <div className="basic">
                    <div className="basic-icon" onClick={toOtherInfo}>
                        <img src={blog.icon || "/imgs/icons/default-icon.png"} alt=""/>
                    </div>
                    <div className="basic-info">
                        <div className="name">{blog.name}</div>
                        <span className="time">{formatDate(blog.createTime)}</span>
                    </div>
                    <div style={{width: "20%"}}>
                        {(!user || user.id !== blog.userId) && (
                            <div className="logout-btn" onClick={follow}>
                                {followed ? "Unfollow" : "Follow"}
                            </div>
                        )}
                    </div>
                </div>

                <div className="blog-text" dangerouslySetInnerHTML={{__html: blog.content}}/>

                <div className="shop-basic">
                    <div className="shop-icon">
                        <img src={shop.image} alt=""/>
                    </div>
                    <div style={{width: "80%"}}>
                        <div className="name">{shop.name}</div>
                        <div>Rating: {(shop.score / 10).toFixed(1)}</div>
                        <div className="shop-avg">￥{shop.avgPrice} / person</div>
                    </div>
                </div>

                <div className="zan-box">

                    <div>
                        <svg t="1646634642977" className="icon" viewBox="0 0 1024 1024" version="1.1"
                             xmlns="http://www.w3.org/2000/svg" p-id="2187" width="20" height="20">
                            <path
                                d="M160 944c0 8.8-7.2 16-16 16h-32c-26.5 0-48-21.5-48-48V528c0-26.5 21.5-48 48-48h32c8.8 0 16 7.2 16 16v448zM96 416c-53 0-96 43-96 96v416c0 53 43 96 96 96h96c17.7 0 32-14.3 32-32V448c0-17.7-14.3-32-32-32H96zM505.6 64c16.2 0 26.4 8.7 31 13.9 4.6 5.2 12.1 16.3 10.3 32.4l-23.5 203.4c-4.9 42.2 8.6 84.6 36.8 116.4 28.3 31.7 68.9 49.9 111.4 49.9h271.2c6.6 0 10.8 3.3 13.2 6.1s5 7.5 4 14l-48 303.4c-6.9 43.6-29.1 83.4-62.7 112C815.8 944.2 773 960 728.9 960h-317c-33.1 0-59.9-26.8-59.9-59.9v-455c0-6.1 1.7-12 5-17.1 69.5-109 106.4-234.2 107-364h41.6z m0-64h-44.9C427.2 0 400 27.2 400 60.7c0 127.1-39.1 251.2-112 355.3v484.1c0 68.4 55.5 123.9 123.9 123.9h317c122.7 0 227.2-89.3 246.3-210.5l47.9-303.4c7.8-49.4-30.4-94.1-80.4-94.1H671.6c-50.9 0-90.5-44.4-84.6-95l23.5-203.4C617.7 55 568.7 0 505.6 0z"
                                p-id="2188" fill={blog.isLike ? "#ff6633" : "#82848a"}></path>
                        </svg>
                    </div>
                    <div className="zan-list">
                        {likes.map((u) => (
                            <div className="user-icon-mini" key={u.id}>
                                <img src={u.icon || "/imgs/icons/default-icon.png"} alt=""/>
                            </div>
                        ))}
                        <div style={{marginLeft: 10, textAlign: "center", lineHeight: "24px"}}>
                            {blog.liked} likes
                        </div>
                    </div>
                </div>

                <div className="blog-divider"/>
            </div>

            <div className="foot">
                <div className="foot-box">
                    <div className="foot-view" onClick={addLike}>
                        {/*{blog.isLike ? <LikeFilled style={{ color: '#1890ff' }} /> : <LikeOutlined />}*/}

                        <svg t="1646634642977" className="icon" viewBox="0 0 1024 1024" version="1.1"
                             xmlns="http://www.w3.org/2000/svg" p-id="2187" width="26" height="26">
                            <path
                                d="M160 944c0 8.8-7.2 16-16 16h-32c-26.5 0-48-21.5-48-48V528c0-26.5 21.5-48 48-48h32c8.8 0 16 7.2 16 16v448zM96 416c-53 0-96 43-96 96v416c0 53 43 96 96 96h96c17.7 0 32-14.3 32-32V448c0-17.7-14.3-32-32-32H96zM505.6 64c16.2 0 26.4 8.7 31 13.9 4.6 5.2 12.1 16.3 10.3 32.4l-23.5 203.4c-4.9 42.2 8.6 84.6 36.8 116.4 28.3 31.7 68.9 49.9 111.4 49.9h271.2c6.6 0 10.8 3.3 13.2 6.1s5 7.5 4 14l-48 303.4c-6.9 43.6-29.1 83.4-62.7 112C815.8 944.2 773 960 728.9 960h-317c-33.1 0-59.9-26.8-59.9-59.9v-455c0-6.1 1.7-12 5-17.1 69.5-109 106.4-234.2 107-364h41.6z m0-64h-44.9C427.2 0 400 27.2 400 60.7c0 127.1-39.1 251.2-112 355.3v484.1c0 68.4 55.5 123.9 123.9 123.9h317c122.7 0 227.2-89.3 246.3-210.5l47.9-303.4c7.8-49.4-30.4-94.1-80.4-94.1H671.6c-50.9 0-90.5-44.4-84.6-95l23.5-203.4C617.7 55 568.7 0 505.6 0z"
                                p-id="2188" fill={blog.isLike ? "#ff6633" : "#82848a"}></path>
                        </svg>
                        <span className={blog.isLike ? "liked" : ""}
                              style={{fontSize: "12px", marginLeft: "5px"}}>{blog.liked}</span>


                    </div>
                </div>
                <div style={{width: "40%"}}></div>
                <div className="foot-box">
                    <div className="foot-view">
                        <i className="el-icon-chat-square"></i>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default BlogDetail;
