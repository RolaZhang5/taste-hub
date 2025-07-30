import React, { useEffect, useState, useRef } from "react";
import axios from "/public/js/common.js";
import { SearchOutlined } from '@ant-design/icons';
import { Input } from 'antd';
import { navigateTo } from "../navigateHelper.js";
import FootBar from './FootBar.jsx';
import '/public/css/element.css';
import '/public/css/index.css';
import '/public/css/main.css';

function HomePage() {
  const [types, setTypes] = useState([]);
  const [blogs, setBlogs] = useState([]);
  const [current, setCurrent] = useState(1);
  const currentRef = useRef(1);
  const [isReachBottom, setIsReachBottom] = useState(false);
  const blogListRef = useRef();
  const [activeBtn, setActiveBtn] = useState(1);

  useEffect(() => {
    fetchShopTypes();
    fetchHotBlogs();
  }, []);

  const fetchShopTypes = async () => {
    try {
      const { data } = await axios.get("/shop-type/list");
      setTypes(data);
    } catch (err) {
      alert(err);
    }
  };

  const fetchHotBlogs = async () => {
    try {
      const { data } = await axios.get(`/blog/hot?current=${currentRef.current}`);
      data.forEach((b) => (b.img = b.images.split(",")[0]));
      setBlogs((prev) => [...prev, ...data]);
    } catch (err) {
      alert(err);
    }
  };

  const addLike = async (b) => {
    try {
      await axios.put(`/blog/like/${b.id}`);
      fetchBlogById(b);
    } catch (err) {
      alert(err);
    }
  };

  const fetchBlogById = async (b) => {
    try {
      const { data } = await axios.get(`/blog/${b.id}`);
      b.liked = data.liked;
      b.isLike = data.isLike;
      setBlogs([...blogs]);
    } catch (err) {
      alert(err);
      b.liked++;
      setBlogs([...blogs]);
    }
  };

  const handleScroll = (e) => {
    const { scrollTop, offsetHeight, scrollHeight } = e.target;
    if (scrollTop + offsetHeight > scrollHeight && !isReachBottom) {
      setIsReachBottom(true);
      currentRef.current += 1;
      setCurrent(currentRef.current);
      fetchHotBlogs();
    } else {
      setIsReachBottom(false);
    }
  };

  const navigateToShopList = (id, name) => {
    navigateTo(`/shop-list?type=${id}&name=${name}`);
  };

  const navigateToBlogDetail = (b) => {
    navigateTo(`/blog-detail?id=${b.id}`);
  };

  return (
      <div id="app" >
        <div className="search-bar">
          <div className="city-btn">
            Sydney<i className="el-icon-arrow-down"></i>
          </div>
          <div className="search-input">
            <SearchOutlined style={{ color: 'grey' }} />
            <Input placeholder="Enter shop name or location" style={{ borderRadius: '20px' }} />
          </div>
          <div className="header-icon" onClick={() => navigateTo('/my-info')}>
            <i className="el-icon-user"></i>
          </div>
        </div>

        <div className="type-list">
          {types.map((t) => (
              <div key={t.id} className="type-box" onClick={() => navigateToShopList(t.id, t.name)}>
                <div className="type-view">
                  <img src={`/imgs/${t.icon}`} alt="" />
                </div>
                <div className="type-text">{t.name}</div>
              </div>
          ))}
        </div>

        <div className="blog-list" onScroll={handleScroll} ref={blogListRef}>
          {blogs.map((b) => (
              <div className="blog-box" key={b.id}>
                <div className="blog-img" onClick={() => navigateToBlogDetail(b)}>
                  <img src={b.img} alt="" />
                </div>
                <div className="blog-title">{b.title}</div>
                <div className="blog-foot">
                  <div className="blog-user-icon">
                    <img src={b.icon || "/imgs/icons/default-icon.png"} alt="" />
                  </div>
                  <div className="blog-user-name">{b.name}</div>
                  <div className="blog-liked" onClick={() => addLike(b)}>
                    <svg
                        className="icon"
                        viewBox="0 0 1024 1024"
                        width="14"
                        height="14"
                    >
                      <path
                          d="M160 944c0 8.8-7.2 16-16 16h-32c-26.5 0-48-21.5-48-48V528c0-26.5
                      21.5-48 48-48h32c8.8 0 16 7.2 16 16v448zM96 416c-53 0-96 43-96 96v416c0
                      53 43 96 96 96h96c17.7 0 32-14.3
                      32-32V448c0-17.7-14.3-32-32-32H96zM505.6
                      64c16.2 0 26.4 8.7 31 13.9 4.6 5.2 12.1
                      16.3 10.3 32.4l-23.5 203.4c-4.9 42.2 8.6 84.6
                      36.8 116.4 28.3 31.7 68.9 49.9
                      111.4 49.9h271.2c6.6 0 10.8 3.3 13.2 6.1s5
                      7.5 4 14l-48 303.4c-6.9 43.6-29.1
                      83.4-62.7 112C815.8 944.2 773 960 728.9
                      960h-317c-33.1 0-59.9-26.8-59.9-59.9v-455c0-6.1
                      1.7-12 5-17.1 69.5-109 106.4-234.2
                      107-364h41.6z"
                          fill={b.isLike ? "#ff6633" : "#82848a"}
                      ></path>
                    </svg>
                    {b.liked}
                  </div>
                </div>
              </div>
          ))}
        </div>
        <div className="blog-divider"/>
        <FootBar activeBtn={activeBtn} onChange={(i) => setActiveBtn(i)} />
      </div>
  );
}

export default HomePage;
