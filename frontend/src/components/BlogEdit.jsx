import React, {useEffect, useRef, useState} from 'react';
import axios from "/public/js/common.js";
import styles from '/public/css/blog-edit.module.css';
import {navigateTo} from "../navigateHelper.js";

const BlogEdit = () => {
    const fileInputRef = useRef(null);
    const [fileList, setFileList] = useState([]);
    const [params, setParams] = useState({});
    const [showDialog, setShowDialog] = useState(false);
    const [shops, setShops] = useState([]);
    const [shopName, setShopName] = useState('');
    const [selectedShop, setSelectedShop] = useState({});

    useEffect(() => {
        checkLogin();
        queryShops();
        console.log("fileList changed:", fileList);
    }, [fileList]);

    const checkLogin = () => {
        let token = sessionStorage.getItem("token");
        if (!token) {
            navigateTo('/login');
        }
        axios.get("/user/me")
            .catch(err => {
                alert(err);
                setTimeout(() => navigateTo('/login'), 200);
            });
    };

    const goBack = () => {
        history.back();
    };

    const queryShops = () => {
        axios.get(`/shop/of/name?name=${shopName}`)
            .then(({data}) => setShops(data))
            .catch(err => alert(err));
    };

    const selectShop = (shop) => {
        setSelectedShop(shop);
        setShowDialog(false);
    };

    const openFileDialog = () => {
        fileInputRef.current.click();
    };

    const fileSelected = () => {
        const file = fileInputRef.current.files[0];
        const formData = new FormData();
        formData.append("file", file);
        const config = {
            headers: {"Content-Type": `multipart/form-data;boundary=${new Date().getTime()}`}
        };
        axios.post("/upload/blog", formData, config)
            .then(({data}) => {
                setTimeout(() => {
                    setFileList(prev => [...prev, '/imgs' + data]);
                }, 300);
            })
            .catch(err => alert(err));
    };

    const deletePic = (index) => {
        axios.get(`/upload/blog/delete?name=${fileList[index]}`)
            .then(() => setFileList(prev => prev.filter((_, i) => i !== index)))
            .catch(err => alert(err));
    };

    const submitBlog = () => {
        const data = {...params};
        data.images = fileList.join(",");
        data.shopId = selectedShop.id;
        axios.post("/blog", data)
            .then(() => navigateTo('/my-info'))
            .catch(err => alert(err));
    };

    return (
        <div className="app">
            <div className={styles["header"]}>
                <div className={styles['header-cancel-btn']} onClick={goBack}>Cancel</div>
                <div className={styles['header-title']}>New Note<i className="el-icon-info"></i></div>
                <div className={styles["header-commit"]}>
                    <div className={styles['header-commit-btn']} onClick={submitBlog}>Publish</div>
                </div>
            </div>

            <div className={styles["upload-box"]}>
                <input type="file" onChange={fileSelected} ref={fileInputRef} style={{display: 'none'}}/>
                <div className={styles["upload-btn"]} onClick={openFileDialog}>
                    <i className="el-icon-camera"></i>
                    <div style={{fontSize: 12, lineHeight: '12px'}}>Upload Photo</div>
                </div>
                <div className={styles["pic-list"]}>
                    {fileList.map((f, i) => (
                        <div className={styles["pic-box"]} key={i}>
                            <img
                                src={f}
                                onLoad={(e) => {
                                    console.log("Loaded:", e.target.src);
                                }}
                                onError={(e) => {
                                    console.error("Failed to load:", e.target.src);
                                }}
                                style={{maxWidth: 200}}
                                alt="uploaded"
                            />
                            <i className="el-icon-close" onClick={() => deletePic(i)}></i>
                        </div>
                    ))}
                </div>
            </div>

            <div className={styles["blog-title"]}>
                <input
                    type="text"
                    placeholder="Enter a title to help it reach the homepage"
                    value={params.title || ''}
                    onChange={e => setParams({...params, title: e.target.value})}
                />
            </div>

            <div className={styles["blog-content"]}>
                <textarea
                    placeholder="Where have you been recently? Any interesting experiences?"
                    value={params.content || ''}
                    onChange={e => setParams({...params, content: e.target.value})}
                ></textarea>
            </div>

            <div className={styles["divider"]}></div>

            <div className={styles["blog-shop"]} onClick={() => setShowDialog(true)}>
                <div className={styles["shop-left"]}>Related Shop</div>
                <div>{selectedShop.name ? selectedShop.name : <>Select&nbsp;<i className="el-icon-arrow-right"></i></>}</div>
            </div>

            {showDialog && <div className="mask" onClick={() => setShowDialog(false)}></div>}

            {showDialog && (
                <div className={styles["shop-dialog"]}>
                    <div className={styles["blog-shop"]}>
                        <div className={styles["shop-left"]}>Related Shop</div>
                    </div>
                    <div className={styles["search-bar"]}>
                        <div className={styles["city-select"]}>Hangzhou <i className="el-icon-arrow-down"></i></div>
                        <div className={styles["search-input"]}>
                            <i className="el-icon-search" onClick={queryShops}></i>
                            <input
                                type="text"
                                placeholder="Search shop name"
                                value={shopName}
                                onChange={e => setShopName(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className={styles["shop-list"]}>
                        {shops.map((s, i) => (
                            <div key={i} className={styles["shop-item"]} onClick={() => selectShop(s)}>
                                <div className={styles["shop-name"]}>{s.name}</div>
                                <div>{s.area}</div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default BlogEdit;
