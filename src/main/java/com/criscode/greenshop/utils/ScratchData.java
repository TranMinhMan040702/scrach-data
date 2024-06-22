package com.criscode.greenshop.utils;

import com.criscode.greenshop.dto.ProductDto;
import com.criscode.greenshop.dto.ProductImageDto;
import com.criscode.greenshop.dto.VariantDto;
import com.criscode.greenshop.entity.ProductCategory;
import com.criscode.greenshop.entity.Unit;
import com.criscode.greenshop.repository.ProductCategoryRepository;
import com.criscode.greenshop.repository.ProductRepository;
import com.criscode.greenshop.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ScratchData {

    private final static String URL_BASE = "https://ecoeshop.vn";
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final UnitRepository unitRepository;

    private List<VariantDto> variantDtos = new ArrayList<>();
    private List<ProductDto> productDtos = new ArrayList<>();
    private List<ProductImageDto> productImages = new ArrayList<>();

    public void run() {
        System.out.println("===========START TOOL==========");

        getProduct();

        System.out.println("===========EXPORT CSV PRODUCT==========");
        exportProductCSV();

        System.out.println("===========EXPORT CSV PRODUCT IMAGE==========");
        exportProductImageCSV();

        System.out.println("===========EXPORT CSV VARIANT==========");
        exportVariantCSV();

        System.out.println("===========END TOOL==========");
    }

    private void getProduct() {
        try {
            Map<String, List<String>> map = getLinkProducts();
            Long maxId = productRepository.findMaxId();
            long maxIdImage = 28L;
            long maxIdVariant = 25L;


            System.out.println("===========START GET PRODUCT==========");
            for (Map.Entry<String, List<String>> item : map.entrySet()) {

                System.out.println("===========CATEGORY==========>" + item.getKey());

                ProductCategory productCategory =
                        productCategoryRepository.findProductCategoryBySlug(item.getKey()
                                .replaceAll("/collections/", ""));

                if (productCategory == null) {
                    continue;
                }

                List<String> urlProducts = item.getValue();

                for (String url : urlProducts) {
                    System.out.println("===========PRODUCT==========>" + url);
                    Document document = null;
                    try {
                        document = Jsoup.connect(URL_BASE + url).get();
                    } catch (Exception e) {
                        continue;
                    }

                    // TODO: get element have image
                    Element elementWrapbox = document.getElementsByClass("wrapbox-image")
                            .stream()
                            .findAny()
                            .get();

                    // TODO: get name product
                    String name = document.getElementsByClass("product-heading")
                            .stream()
                            .findFirst()
                            .get()
                            .getElementsByTag("h1")
                            .text();

                    // TODO: check product exist
                    if (productRepository.existsByName(name)) {
                        continue;
                    }

                    // TODO: get short des
                    Element eDes = document.getElementsByClass("description-productdetail").first();
                    Element eShortDescription = eDes != null ? eDes.selectFirst("p") : null;
                    String shortDescription = eShortDescription != null ? eShortDescription.text() : name;

                    // TODO: get des
                    String des = eDes != null ? eDes.toString() : name;

                    // TODO: get code
                    String code = generateCode(name);

                    // TODO: get slug
                    String[] slug = url.split("/");

                    // TODO: get brand
                    Random rand = new Random();
                    int brandId = rand.nextInt((16 - 1) + 1) + 1;

                    // TODO: get categoryId
                    Long categoryId = productCategory.getId();

                    // TODO: get unitId
                    Element eUnit = document.getElementsByClass("select-swap").stream().findFirst().get();
                    String[] s = eUnit.children().get(0).attr("data-value").split(" ");
                    String unitName = s[s.length - 1].replaceAll("^[0-9]", "").toLowerCase();
                    Unit unit = unitRepository.findByName(unitName);
                    if (unit == null) {
                        continue;
                    }
                    Long unitId = unitRepository.findByName(unitName).getId();

                    // TODO: get count product in db
                    ProductDto product = ProductDto.builder()
                            .id(++maxId)
                            .created_at("2023-11-18 15:43:48")
                            .updated_at("2023-12-29 02:18:03")
                            .created_by(null)
                            .updated_by(null)
                            .name(name)
                            .short_description(!shortDescription.isEmpty() ? shortDescription : name)
                            .description(des != null ? des : name)
                            .code(code)
                            .quantity(2000L)
                            .actual_inventory(2000L)
                            .sold(0L)
                            .rating(5d)
                            .slug(slug[slug.length - 1])
                            .cost(BigDecimal.ZERO)
                            .status("ACTIVE")
                            .product_category_id(categoryId)
                            .brand_id((long) brandId)
                            .unit_id(unitId)
                            .build();

                    // TODO: get images
                    List<String> images = getImages(elementWrapbox);

                    for (int i = 0; i < images.size(); i++) {
                        productImages.add(ProductImageDto.builder()
                                .id(++maxIdImage)
                                .created_at("2023-11-18 15:43:48")
                                .updated_at("2023-12-29 02:18:03")
                                .created_by(null)
                                .updated_by(null)
                                .image("https:" + images.get(i))
                                .size(null)
                                .content_type(null)
                                .is_default(i == 0)
                                .product_id(product.id())
                                .build());
                    }

                    // TODO: get variants
                    Element eVariant = document.getElementsByClass("select-swap").stream().findFirst().get();
                    Elements listVariant = eVariant.children();
                    int count = 1;

                    for (Element variant : listVariant) {
                        long quantity;
                        BigDecimal itemPrice;
                        String[] value = variant.attr("data-value").split(" ");

                        if (value.length == 3) {
                            quantity = Long.parseLong(value[2].replaceAll("[A-z]", ""));
                        } else {
                            quantity = Long.parseLong(value[2]);
                        }

                        BigDecimal price = BigDecimal.valueOf(Long.parseLong(variant.attr("data-price"))).divide(BigDecimal.valueOf(100));

                        itemPrice = price.divide(BigDecimal.valueOf(quantity));

                        variantDtos.add(VariantDto.builder()
                                .id(++maxIdVariant)
                                .created_at("2023-11-18 15:43:48")
                                .updated_at("2023-12-29 02:18:03")
                                .created_by(null)
                                .updated_by(null)
                                .name(value[0])
                                .sku(product.code() + "-" + count++)
                                .quantity(quantity)
                                .item_price(itemPrice)
                                .total_price(price)
                                .promotional_item_price(null)
                                .total_promotional_price(null)
                                .status("ACTIVE")
                                .product_id(product.id())
                                .build());
                    }

                    productDtos.add(product);
                }
            }

            System.out.println("===========END GET PRODUCT===========");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<String> getImages(Element pElement) {
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

    private List<String> getLinkCategories() {
        try {
            List<String> urlCategories = new ArrayList<>();

            Document document = Jsoup.connect(URL_BASE).get();

            Elements elements = document.getElementsByClass("htp-tablink").addClass("hihilink");

            for (Element el : elements) {
                String url = el.getElementsByTag("a").attr("href");
                urlCategories.add(url);
            }

            return urlCategories;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Map<String, List<String>> getLinkProducts() {
        try {
            Map<String, List<String>> map = new HashMap<>();
            List<String> urlCategories = getLinkCategories();

            for (String urlCate : urlCategories) {
                List<String> urlProducts = new ArrayList<>();
                Document document = Jsoup.connect(URL_BASE + urlCate).get();

                Elements elements = document.getElementsByClass("product-inner").addClass("product-resize");

                for (Element element : elements) {
                    String url = element.getElementsByTag("a").attr("href");
                    urlProducts.add(url);
                }

                map.put(urlCate, urlProducts);
            }

            return map;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void exportProductCSV() {
        String csvFile = "product.csv";
        String[] HEADER = {"id", "created_at", "updated_at", "created_by", "updated_by", "name", "short_description",
                "description", "code", "quantity", "actual_inventory", "sold",
                "rating", "slug", "cost", "status", "product_category_id", "brand_id", "unit_id"};

        try (FileWriter writer = new FileWriter(csvFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(HEADER))) {

            for (ProductDto product : productDtos) {
                csvPrinter.printRecord(product.id()
                        , product.created_at()
                        , product.updated_at()
                        , product.created_by()
                        , product.updated_by()
                        , product.name()
                        , product.short_description()
                        , product.description()
                        , product.code()
                        , product.quantity()
                        , product.actual_inventory()
                        , product.sold()
                        , product.rating()
                        , product.slug()
                        , product.cost()
                        , product.status()
                        , product.product_category_id()
                        , product.brand_id()
                        , product.unit_id());
            }

            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportProductImageCSV() {
        String csvFile = "product_image.csv";
        String[] HEADER = {"id", "created_at", "updated_at", "created_by", "updated_by", "image", "size",
                "content_type", "is_default", "product_id"};

        try (FileWriter writer = new FileWriter(csvFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(HEADER))) {

            for (ProductImageDto imageDto : productImages) {
                csvPrinter.printRecord(imageDto.id()
                        , imageDto.created_at()
                        , imageDto.updated_at()
                        , imageDto.created_by()
                        , imageDto.updated_by()
                        , imageDto.image()
                        , imageDto.size()
                        , imageDto.content_type()
                        , imageDto.is_default()
                        , imageDto.product_id());
            }

            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportVariantCSV() {
        String csvFile = "variant.csv";
        String[] HEADER = {"id", "created_at", "updated_at", "created_by", "updated_by", "name", "sku", "quantity", "item_price", "total_price", "promotional_item_price",
                "total_promotional_price", "status", "product_id"};

        try (FileWriter writer = new FileWriter(csvFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(HEADER))) {

            for (VariantDto variantDto : variantDtos) {
                csvPrinter.printRecord(variantDto.id()
                        , variantDto.created_at()
                        , variantDto.updated_at()
                        , variantDto.created_by()
                        , variantDto.updated_by()
                        , variantDto.name()
                        , variantDto.sku()
                        , variantDto.quantity()
                        , variantDto.item_price()
                        , variantDto.total_price()
                        , variantDto.promotional_item_price()
                        , variantDto.total_promotional_price()
                        , variantDto.status()
                        , variantDto.product_id());
            }

            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateCode(String obj) {
        if (obj != null && !obj.isEmpty()) {

            String newInput = removeDiacriticalMarks(obj);

            String newObj = newInput.replaceAll("\\s*\\([^\\)]*\\)", "");

            String[] words = newObj.split(" ");

            StringBuilder acronym = new StringBuilder();

            for (String word : words) {
                if (!word.isEmpty()) {
                    acronym.append(word.charAt(0));
                }
            }

            Random random = new Random();
            int randomNumber = random.nextInt(900) + 100;

            return acronym.toString().toUpperCase() + randomNumber;
        }
        return null;
    }

    private static String removeDiacriticalMarks(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    private void test() throws IOException {
        Document document = Jsoup.connect("https://ecoeshop.vn/products/ly-giay-6-5oz-180ml-upc001").get();
    }
}
