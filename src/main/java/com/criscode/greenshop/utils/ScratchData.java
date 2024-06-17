package com.criscode.greenshop.utils;

import com.criscode.greenshop.dto.ProductDto;
import com.criscode.greenshop.entity.Product;
import com.criscode.greenshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScratchData {

    private final static String URL_BASE = "XXXXX";
    private final ProductRepository productRepository;

    public List<String> getLinkCategories() {
        try {
            List<String> urlCategories = new ArrayList<>();

            Document document = Jsoup.connect(URL_BASE).get();

            Elements elements = document.getElementsByClass("htp-tablink").addClass("hihilink");

            for (Element el : elements) {
                String url = el.getElementsByTag("a").attr("href");
                urlCategories.add(URL_BASE + url);
            }

            return urlCategories;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<String> getLinkProducts() {
        try {
            List<String> urlProducts = new ArrayList<>();
            List<String> urlCategories = getLinkCategories();

            for (String urlCate : urlCategories) {
                Document document = Jsoup.connect(urlCate).get();

                Elements elements = document.getElementsByClass("product-inner").addClass("product-resize");

                for (Element element : elements) {
                    String url = element.getElementsByTag("a").attr("href");
                    urlProducts.add(URL_BASE + url);
                }
            }

            return urlProducts;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<String> getImages(Element pElement) {
        try {
            List<String> images = new ArrayList<>();

            Elements slider = pElement.getElementsByClass("productGallery_slider");
            Elements image = null;

            if (!slider.isEmpty()) {
                for (Element sd : slider) {
                    image = sd.getElementsByTag("img");
                }
            } else {
                image = pElement.getElementsByTag("img");
            }

            for (Element e : image) {
                String src = e.attr("src");
                images.add(src);
            }

            return images;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void getData() {
        try {
            List<String> urlProducts = getLinkProducts();
            List<ProductDto> productDtos = new ArrayList<>();
            Long maxId = productRepository.findMaxId();

            for (String url : urlProducts) {
                System.out.println("===========Start get data with==========" + url);

                Document document = Jsoup.connect(url).get();
                Element elementWrapbox = document.getElementsByClass("wrapbox-image")
                        .stream()
                        .findAny()
                        .get();

                String name = document.getElementsByClass("product-heading")
                        .stream()
                        .findFirst()
                        .get()
                        .getElementsByTag("h1")
                        .text();

                if (productRepository.findByName(name)) {
                    continue;
                }

                // TODO: get count product in db
                Product product = Product.builder()
                        .id(maxId++)
                        .name(name)
                        .build();

                // TODO: create new product

                // TODO: get images
                List<String> images = getImages(elementWrapbox);

                // TODO: create list product image

                // TODO: create variant
            }


            System.out.println("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
