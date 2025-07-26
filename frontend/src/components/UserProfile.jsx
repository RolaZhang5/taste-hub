import React, {useEffect, useState, useRef} from "react";
import axios from "/public/js/common.js";
import {Tabs, Input} from "antd";
import {navigateTo} from "../navigateHelper.js";
import FootBar from './FootBar.jsx'
import '/public/css/element.css';
import "/public/css/main.css";
import styles from '/public/css/info.module.css';

const UserProfile = () => {
    const [user, setUser] = useState({});
    const [info, setInfo] = useState({});
    const [blogs, setBlogs] = useState([]);
    const [blogs2, setBlogs2] = useState([]);
    const [activeName, setActiveName] = useState("1");
    const [params, setParams] = useState({minTime: 0, offset: 0});
    const [isReachBottom, setIsReachBottom] = useState(false);
    const [activeBtn, setActiveBtn] = useState(4);
    const contentRef = useRef(null);

    useEffect(() => {
        queryUser();
    }, []);

    const queryUser = () => {
        axios
            .get("/user/me")
            .then(({data}) => {
                setUser(data);
                queryUserInfo(data.id);
                queryBlogs();
            })
            .catch(() => {
                navigateTo('/login');
            });
    };

    const queryUserInfo = async (id) => {
        try {
            const {data} = await axios.get("/user/info/" + id);
            setInfo(data);
            sessionStorage.setItem("userInfo", JSON.stringify(data));
        } catch (err) {
            alert(err);
        }
    };

    const queryBlogs = async () => {
        try {
            const {data} = await axios.get("/blog/of/me");
            setBlogs(data);
        } catch (err) {
            alert(err);
        }
    }

    const queryBlogsOfFollow = (clear = false) => {
        let queryParams = {...params};
        if (clear) {
            queryParams = {
                minTime: new Date().getTime() + 1,
                offset: 0,
            };
        }
        axios
            .get("/blog/of/follow", {params: {offset: queryParams.offset, lastId: queryParams.minTime}})
            .then(({data}) => {
                if (!data) return;
                const {list, ...newParams} = data;
                list.forEach(b => b.img = b.images.split(",")[0]);
                setBlogs2(clear ? list : blogs2.concat(list));
                setParams(newParams);
            })
            .catch(console.log);
    };

    const handleTabClick = (tab) => {
        setActiveName(tab);
        if (tab === "4") {
            queryBlogsOfFollow(true);
        }
    };

    const addLike = async (b) => {
        try {
            await axios.put("/blog/like/" + b.id);
            await queryBlogById(b)
        } catch (err) {
            alert(err);
        }
    }
    const queryBlogById = (b) => {
        axios
            .get("/blog/" + b.id)
            .then(({data}) => {
                b.liked = data.liked;
                b.isLike = data.isLike;
                setBlogs2([...blogs2]);
            })
            .catch(() => {
                b.liked++;
                setBlogs2([...blogs2]);
            });
    };

    const onScroll = (e) => {
        const scrollTop = e.target.scrollTop;
        const offsetHeight = e.target.offsetHeight;
        const scrollHeight = e.target.scrollHeight;

        if (scrollTop === 0) {
            queryBlogsOfFollow(true);
        } else if (scrollTop + offsetHeight + 1 > scrollHeight && !isReachBottom) {
            setIsReachBottom(true);
            queryBlogsOfFollow();
        } else {
            setIsReachBottom(false);
        }
    };

    const logout = () => {
        axios
            .post("/user/logout")
            .then(() => {
                sessionStorage.removeItem("token");
                window.location.href = "/";
            })
            .catch(err => alert(err));
    };

    const items = [
        {
            key: '1',
            label: 'Notes',
            children: blogs.map(b => (
                <div key={b.id} className={styles["blog-item"]}>
                    <div className={styles["blog-img-profile"]}><img src={b.images.split(',')[0]} alt=""/></div>
                    <div className={styles["blog-info"]}>
                        <div className={styles["blog-title"]}>{b.title}</div>
                        <div className={styles["blog-liked"]}><img src="/imgs/thumbup.png" alt=""/> {b.liked}</div>
                        <div className={styles["blog-comments"]}><i
                            className={styles["el-icon-chat-dot-round"]}></i> {b.comments}</div>
                    </div>
                </div>
            ))
        },
        {
            key: '2',
            label: "Reviews",
            children: <div>Reviews</div>
        },
        {
            key: '3',
            label: "Followers (0)",
            children: <div>Followers (0)</div>
        },
        {
            key: '4',
            label: 'Following (0)',
            children: <div className={styles["blog-list"]}>
                {blogs2.map(b => (
                    <div key={b.id} className="blog-box">
                        <div className="blog-img2"
                             onClick={() =>  navigateTo(`/blog-detail?id=${b.id}`)}><img
                            src={b.img} alt=""/></div>
                        <div className="blog-title">{b.title}</div>
                        <div className="blog-foot">
                            <div className="blog-user-icon"><img
                                src={b.icon || "/imgs/icons/default-icon.png"} alt=""/></div>
                            <div className="blog-user-name">{b.name}</div>
                            <div className="blog-liked" onClick={() => addLike(b)}>
                                <svg className="icon" viewBox="0 0 1024 1024" width="14" height="14">
                                    <path
                                        d="M160 944c0 8.8-7.2 16-16 16h-32c-26.5 0-48-21.5-48-48V528c0-26.5 21.5-48 48-48h32c8.8 0 16 7.2 16 16v448z..."
                                        fill={b.isLike ? '#ff6633' : '#82848a'}/>
                                </svg>
                                {b.liked}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        }
    ]

    return (
        <div className="page" onScroll={onScroll} ref={contentRef}>
            <div className={styles["header"]}>
                <div className="header-back-btn" onClick={() => window.history.back()}><i
                    className="el-icon-arrow-left"></i></div>
                <div className={styles["header-title"]}>User Profile</div>
            </div>

            <div className={styles["basic"]}>
                <div className={styles["basic-icon"]}>
                    <img src={user.icon || "/imgs/icons/default-icon.png"} alt=""/>
                </div>
                <div className={styles["basic-info"]}>
                    <div className={styles["name"]}>{user.nickName}</div>
                    <span>Hangzhou</span>
                    <div className={styles["edit-btn"]} onClick={() => (window.location.href = 'info-edit.html')}>Edit Profile</div>
                </div>
                <div className={styles["logout-btn"]} onClick={logout}>Logout</div>
            </div>

            <div className={styles["introduce"]}>
                {info?.introduce ? <span>{info.introduce}</span> :
                    <span>Add a personal bio to let others know you better <i className={styles["el-icon-edit"]}></i></span>}
            </div>
            <Tabs items={items} activeKey={activeName} onChange={handleTabClick}/>
            {/* Footer component */}
            <div>
                <FootBar activeBtn={activeBtn} onChange={(i) => setActiveBtn(i)}></FootBar>
            </div>
        </div>
    );
};

export default UserProfile;
