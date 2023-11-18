package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.ImageComparisonUtil;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import io.qameta.allure.Attachment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.OutputType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ScreenTests {


    @Test
    public void testScreenIphone12Pro(TestInfo info) {
        Configuration.browserSize = "390*844";
        Selenide.open("https://makeup.com.ua/ua/");
        assertScreen(info);


    }

    public void assertScreen(TestInfo info) {
        // ожидаемое имя файла
        String expectedFileName = info.getTestMethod().get().getName()+ ".png";
        // ожидаемое имя папки где хранятся скриншоты
        String expectedScreenDir = "C:\\Users\\User\\IdeaProjects\\Layout\\src\\test\\resources\\screens\\";

        //  Два файла для сравнения
        //  скриншоты экрана в разных форматах
        File actualScreenShout = Selenide.screenshot(OutputType.FILE);
        // эталонные скриншоты экрана в разных форматах
        File expectedScreenShout = new File(expectedScreenDir + expectedFileName);
        System.out.println(expectedScreenShout);
        // Если нет ожидаемого скриншота в Allure добавим скриншот который только что получили
        //для того что бы его скачать и закинуть в ресурсы
        if (!expectedScreenShout.exists()) {
            //передаем актуальный скриншот
            addImageToAllure("actual", actualScreenShout);
            //нет смысла выполнять код если не с чем сравнить
            throw new IllegalArgumentException("Can't assert image, because there is not reference"
                    + "Actual screen can be downloaded from allure");
        }
        //читаем картинку из ресурсов
        BufferedImage expectedImage = ImageComparisonUtil
                .readImageFromResources(expectedScreenDir + expectedFileName);
        //читаем картинку из Selenide обращаемся к актуальному скриншоту
        //вызываем метод получить путь и превращаем в строрчку
        BufferedImage actualImage = ImageComparisonUtil
                .readImageFromResources(actualScreenShout.toPath().toString());

        //фал показывает несоответствие между expectedImage и actualImage
        File resultDestination = new File("build/diffs/diff_" + expectedFileName);

        //Сравниваем картинки
        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage, resultDestination);
        //Результат сравнения
        ImageComparisonResult result = imageComparison.compareImages();

        //логика сравнения если результат не равен актуальному файлу помещаем 3 файла
        if (!result.getImageComparisonState().equals(ImageComparisonState.MATCH)) {
            addImageToAllure("actual", actualScreenShout);
            addImageToAllure("expected", expectedScreenShout);
            addImageToAllure("diff", resultDestination);
        }
        Assertions.assertEquals(ImageComparisonState.MATCH, result.getImageComparisonState());
    }


    // название файла и сам файл
    // метод превратит файл в байты и прекрепит в отчет
    private void addImageToAllure(String name, File file) {
        try {
            // из файла нужно достать байты
            byte[] image = Files.readAllBytes(file.toPath());
            // передаем name из внешнего аргумента и прочитанные байты
            screenShots(name, image);
        } catch (IOException e) {
            throw new RuntimeException("Can't read bytes as file is absent");
        }
//        screenShots(name, image);
    }

    // название скришота и attachment
    // метод добаввит файл в allure отчет
    @Attachment(value = "{name}", type = "image/png")
    private static byte[] screenShots(String name, byte[] image) {
        return image;
    }


}
