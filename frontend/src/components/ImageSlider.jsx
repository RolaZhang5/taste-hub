import React from "react";
import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";

const ImageSlider = ({ images = [] }) => {
    if (!images || images.length === 0) return null;

    const settings = {
        dots: true,
        infinite: true,  // 只有图片数大于 2 才使用循环
        speed: 500,
        slidesToShow: 1,
        slidesToScroll: 1,
        arrows: false,               // 去掉前后按钮
        swipe: true,
        autoplay: false,
        initialSlide: 0,
        autoplaySpeed: 3000,
    };

    return (
        <div style={{ width: "100%", overflow: "hidden" }}>
            <Slider {...settings}>
                {images.map((img, index) => (
                    <div key={index}>
                        <img
                            src={img}
                            alt={`slide-${index}`}
                            style={{
                                width: "100%",
                                height: "100%",
                                // height: "300px",
                                objectFit: "contain",
                                display: "block"
                            }}
                            onError={(e) => {
                               console.log("加载失败", img)
                                e.target.src = "/imgs/icons/default-icon.png"; // 可选的备用图
                            }}
                        />
                    </div>
                ))}
            </Slider>
        </div>
    );
};

export default ImageSlider;
