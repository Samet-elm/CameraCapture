//Samet ELMALI - 22120205060
//İbrahim Emre ÇELENLİ - 22120205061
//Battal Doğukan AZAR - 21120205021

package org.example;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.opencv.videoio.Videoio;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Duration;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

//kamera işlemlerini içeren class
public class Kamera {

    //gerekli değişkenler
    private static final int CAMERA_DEVICE = 0; //kamera ile işlem yapmak için kullanılır
    private final int kameraCihazi;
    private JFrame frame;
    private JPanel panel;
    private JLabel lblFoto;
    private JButton btnCek;
    private boolean cekiyor;
    private BufferedImage foto;

    public Kamera(int kameraCihazi) {
        this.kameraCihazi = kameraCihazi;
        this.cekiyor = false;

        //guiBaslat metodu çalıştırılıyor
        guiBaslat();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            //kameradan sürekli görüntü almak için gerekli işlemleri yapıyor
            public void run() {
                Kamera camera = new Kamera(CAMERA_DEVICE);
            }
        });
    }

    //bu metod ilk arayüzümüzü başlatıyor
    private void guiBaslat() {

        //arayüzün başlığı "kamera uygulaması" olarak ayarlanıyor
        frame = new JFrame("Kamera Uygulaması");

        //çarpıya bastığımızda uygulamanın tamamen kapatılması için varsayılan kapatma işlemi ayarlanıyor
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        //panel isminde bir JPanel nesnesi oluşturuluyor
        panel = new JPanel();

        //ekrana yerleşme düzeni null olarak ayarlanıyor böylece butonları istedğimiz yere konumlandırabiliyoruz
        panel.setLayout(null);

        //lblFoto isminde bir JLabel nesnesi oluşturuluyor
        lblFoto = new JLabel();

        //bu label'ın boyutu ve konumu ayarlanıyor
        lblFoto.setBounds(0, 0, 640, 480);

        //bu label panele ekleniyor
        panel.add(lblFoto);


        //btnCek isminde bir JButton nesnesi oluşturuluyor ve bu butonun üstünde başla yazıyor
        btnCek = new JButton("Başla");

        //butona basılıp basılmadığını kontrol eden action listener
        btnCek.addActionListener(new ActionListener() {

            //gerekli bir değişken
            File dosyaYolu;


            //butona basılırsa olacak işlemler
            @Override
            public void actionPerformed(ActionEvent e) {


                if (!cekiyor) {

                    //çekiyor değişkeninin değeri true olarak değiştiriliyor
                    cekiyor = true;

                    //üstünde başla yazan butonumuzun üstündeki yazı çek olarak değiştiriliyor
                    btnCek.setText("Çek");

                    //cekmeyeBasla metodu çağırılıyor
                    cekmeyeBasla();

                } else {

                    //çekiyor değişkeninin değeri false olarak değiştiriliyor
                    cekiyor = false;

                    //butonun üstündeki yazı tekrardan başla olarak değiştiriliyor
                    btnCek.setText("Başla");

                    //fotoCek metodu çağırılıyor ve return edilen değer foto değişkenine kaydediliyor
                    foto = fotoCek();
                    try {
                        //dosya yoluna fotoğraf kaydediliyor
                        dosyaYolu = fotoKaydet(foto);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                    //ikinci arayüz için bir nesne oluşturuluyor
                    ikinciGUI objGUI = new ikinciGUI();

                    //Yeni oluşturacağımız arayüzün içerik bölmesi katmanına ikinciGUI'deki panel'in içeriği ekleniyor
                    objGUI.setContentPane(objGUI.panel);

                    //fotoğraf için kullanılan lblFoto'ya çektiğimiz fotoğraf konuluyor
                    objGUI.lblFoto.setIcon(new ImageIcon(dosyaYolu.toString() + ".jpg"));

                    //fotoğrafın boyutları size değişkenine kaydediliyor
                    Dimension size = objGUI.lblFoto.getPreferredSize();

                    //lblFoto'nun boyutu (fotoğrafın boyutuna göre) ve konumu ayarlanıyor
                    objGUI.lblFoto.setBounds(50, 30, size.width, size.height);

                    //arayüzün başlığı "Kamera Uygulaması" olarak ayarlanıyor
                    objGUI.setTitle("Kamera Uygulaması");

                    //arayüzün boyutu fotoğrafın boyutuna göre ayarlanıyor
                    objGUI.setSize(size.width + 15, size.height + 250);

                    //arayüzün ekranın tam ortasına gelmesi sağlanıyor
                    objGUI.setLocationRelativeTo(null);

                    //çarpı tuşuna basıldığında arayüzün saklanması sağlanıyor
                    objGUI.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

                    //arayüz ekrana geliyor
                    objGUI.setVisible(true);
                }
            }
        });

        //btnCek'in konumu ve boyutu ayarlanıyor
        btnCek.setBounds(100, 500, 400, 50);

        //panele btnCek ekleniyor
        panel.add(btnCek);

        //arayüzün içerik bölmesi katmanına panel'in içeriği ekleniyor
        frame.getContentPane().add(panel);

        //pencere yeniden boyutlandırılamaz olarak ayarlanıyor (bazı bozulmalar yaşanmasın diye)
        frame.setResizable(false);

        //arayüzün teması değiştiriliyor
        LafManager.install(new DarculaTheme());

        //arayüzün boyutu ayarlanıyor
        frame.setSize(640, 600);

        //arayüzün ekranın tam ortasında başlaması sağlanıyor
        frame.setLocationRelativeTo(null);

        //arayüz ekrana getiriliyor
        frame.setVisible(true);
    }

    private void cekmeyeBasla() {
        // Yeni bir Thread (iş parçacığı) oluşturarak görüntüyü yakalamaya başla
        new Thread(new Runnable() {//çoklu iş yapmaya olanak sağlar
            @Override
            public void run() {
                // OpenCVFrameGrabber nesnesi oluşturuluyor ve kamera cihazı parametresiyle başlatılıyor
                OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(kameraCihazi);

                // Yakalanan görüntünün genişlik ve yükseklik özellikleri ayarlama
                grabber.setOption(Videoio.CAP_PROP_FRAME_WIDTH, 640);
                grabber.setOption(Videoio.CAP_PROP_FRAME_HEIGHT, 480);

                try {
                    grabber.start(); // Görüntü yakalamaya başlama

                    // cekiyor değişkeni true olduğu sürece döngü çalışacak
                    while (cekiyor) {
                        Frame frame = grabber.grab(); // Bir frame yakalanıyor

                        if (frame != null) {
                            // Yakalanan frame Java2DFrameConverter kullanılarak BufferedImage'ye dönüştürülüyor
                            Java2DFrameConverter converter = new Java2DFrameConverter();
                            BufferedImage bufferedImage = converter.getBufferedImage(frame);

                            // BufferedImage üzerinden ImageIcon oluşturuluyor
                            ImageIcon icon = new ImageIcon(bufferedImage);

                            // Etikete (lblfoto) ikon atanıyor, panel yeniden doğrulanıyor ve yeniden çiziliyor
                            lblFoto.setIcon(icon);

                            foto = bufferedImage; // foto değişkeni güncelleniyor
                        }
                    }
                } catch (FrameGrabber.Exception e) {
                    // Kamera cihazı açılırken bir hata oluşursa hata mesajı yazdırılıyor
                    System.err.println("Kamera cihazı açılırken bir hata oluştu: " + e.getMessage());
                } finally {
                    try {
                        grabber.stop(); // Görüntü yakalamayı durdur
                    } catch (FrameGrabber.Exception e) {
                        // Kamera cihazı kapatılırken bir hata oluşursa hata mesajı yazdırılıyor
                        System.err.println("Kamera cihazı kapatılırken bir hata oluştu: " + e.getMessage());
                    }
                }
            }
        }).start(); // Thread'i başlat
    }

    public BufferedImage fotoCek() {
        //foto değişkeni dönülüyor
        return foto;
    }

    public File fotoKaydet(BufferedImage image) throws IOException {

        //fcKaydet isimli bir JFileChooser nesnesi ekleniyor.
        JFileChooser fcKaydet = new JFileChooser();

        //arayüze dosya kaydetme ekranının çıkmasını sağlıyor
        fcKaydet.showSaveDialog(frame);

        //seçilen dosyanın konumu dosyaYolu değişkenine kaydediliyor
        File dosyaYolu = fcKaydet.getSelectedFile();

        //"temp.txt" isimli bir File nesnesi oluşturuluyor
        File fp = new File("temp.txt");

        //dosya okumak için gerekli nesne oluşturuluyor
        FileWriter fw = new FileWriter(fp);

        //dosyaya dosya yolu kaydediliyor
        fw.write(dosyaYolu.toString() + ".jpg");

        //dosya kapatılıyor
        fw.close();


        //dosya yolu ilk dosya ile aynı olan yeni bir dosya oluşturuluyor
        File ciktiDosya = new File(dosyaYolu + ".jpg");

        try {

            //ciktiDosya içerisine fotoğraf kaydediliyor
            ImageIO.write(image, "jpg", ciktiDosya);

            //dosya kaydedilirken hata oluşursa ekrana hata mesajı basılıyor
        } catch (IOException e) {
            System.err.println("Fotoğraf kaydedilirken bir hata oluştu: " + e.getMessage());
        }

        //dosya kaydetme ekranı kapatılıyor
        fcKaydet.setVisible(false);

        //arayüze dosya kaydetme ekranı ekleniyor
        frame.add(fcKaydet);

        //dosyaYolu isimli değişkeni dönüyor
        return dosyaYolu;
    }
}

class ikinciGUI extends JFrame {

    //GUI için gerekli değişkenler
    JPanel panel;
    JButton btnPaylas, btnSBEfekt, btnBulanik, btnNegatif;
    JLabel lblFoto;

    public ikinciGUI() {

        //Paylaş butonuna basılıp basılmadığını kontrol eden action listener
        btnPaylas.addActionListener(new ActionListener() {
            @Override

            //butona basılırsa olacak işlemler
            public void actionPerformed(ActionEvent e) {

                //paylasGUI class'ından bir nesne oluşturuluyor
                paylasGUI objPaylasGUI = new paylasGUI();

                //Yeni oluşturacağımız arayüzün içerik bölmesi katmanına paylasGUI'deki panel'in içeriği ekleniyor
                objPaylasGUI.setContentPane(objPaylasGUI.panel);

                //Arayüzün başlığı "Kamera uygulaması" olarak düzenleniyor
                objPaylasGUI.setTitle("Kamera Uygulaması");

                //Arayüzün büyüklüğü ayarlanıyor
                objPaylasGUI.setSize(300, 150);

                //Arayüzün ekranın tam ortasında olası sağlanıyor
                objPaylasGUI.setLocationRelativeTo(null);

                //Ayaryüzdeki çarpı işasretine tıkladığımızda uygulamanın
                //tamamen kapanması yerine sadece bu arayüzün gizlenmesi sağlanıyor
                objPaylasGUI.setDefaultCloseOperation(HIDE_ON_CLOSE);

                //Arayüz ekrana getiriliyor
                objPaylasGUI.setVisible(true);
            }
        });

        //siyah-beyaz efekt butonuna basılıp basılmadığını kontrol eden action listener
        btnSBEfekt.addActionListener(new ActionListener() {
            @Override

            //butona basılırsa olacak işlemler
            public void actionPerformed(ActionEvent e) {
                sbEfekt();
            }
        });

        //bulanıklaştır butonuna tıklanıp tıklanmadığını kontrol eden action listener
        btnBulanik.addActionListener(new ActionListener() {
            @Override

            //butona basılırsa olacak işlemler
            public void actionPerformed(ActionEvent e) {
                bulaniklastir();
            }
        });

        //negatif efekti butonuna basılıp basılmadığını kontrol eden action listener
        btnNegatif.addActionListener(new ActionListener() {
            @Override

            //butona basılırsa olacak işlemler
            public void actionPerformed(ActionEvent e) {
                negatifEfekt();
            }
        });
    }

    public void negatifEfekt(){

        //gerekli değişken
        String dosyaYolu;

        //temp.txt" dosyasının içindeki dosya konumunu okumak için nesne oluşturuluyor
        File f = new File("temp.txt");

        try {

            //dosya okumak için gerekli nesneler oluşturuluyor ve
            //okunan içerik dosyaYolu değişkenine kaydediliyor
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            dosyaYolu = br.readLine();

            //dosya kapatılıyor
            fr.close();
            br.close();

            //eğer dosya okuma işlemi başarısız olursa olacak olaylar
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        //gerekli değişken
        BufferedImage foto = null;
        try {

            //dosya yolu verilen dosyadaki fotoğrafı okuyup foto değişkenine kaydediyor
            foto = ImageIO.read(new File(dosyaYolu));

            //okuma sırasında hata olursa olacaklar
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        //fotonun genişlik ve yükseklik değerleri kaydediliyor
        int width = foto.getWidth();
        int height = foto.getHeight();

        // Belirli bir genişlik ve yükseklikte boş bir BufferedImage oluşturulur
        // Bu yeni görüntü, negatif efekti uygulanacak fotoğrafın boyutlarını alır
        BufferedImage filtreliFoto = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(foto.getRGB(x, y));  // Her pikselin rengi alınır

                int red = 255 - color.getRed();  // Kırmızı bileşenin negatif değeri hesaplanır
                int green = 255 - color.getGreen();  // Yeşil bileşenin negatif değeri hesaplanır
                int blue = 255 - color.getBlue();  // Mavi bileşenin negatif değeri hesaplanır

                Color newColor = new Color(red, green, blue);  // Yeni renk oluşturulur
                filtreliFoto.setRGB(x, y, newColor.getRGB());  // Yeni renk, belirtilen konuma yerleştirilir
            }
        }

        //dosya yolu ilk fotoğrafla aynı olan bir çıktı dosya nesnesi üretiliyor
        File outputFile = new File(dosyaYolu);
        try {

            //filtreli fotoğrafı .jpg formatında çıktı dosyasa kaydediyor
            ImageIO.write(filtreliFoto, "jpg", outputFile);

            // bir hata olursa ekrana hata mesajı yazdırılıyor
        } catch (IOException x) {
            System.err.println("Fotoğraf kaydedilirken bir hata oluştu: " + x.getMessage());
        }

        //Arayüzdeki fotoğraf için kullanılan label'ın içeriğini yeni filtreli fotoğrafımız ile değiştiriyor
        lblFoto.setIcon(new ImageIcon(filtreliFoto));
    }
    public void sbEfekt(){
        //Gerekli bir değişken
        String dosyaYolu;

        //"temp.txt" dosyasının içindeki dosya konumunu okumak için nesne oluşturuluyor
        File f = new File("temp.txt");

        try {

            //dosya okumak için gerekli nesneler oluşturuluyor ve
            //okunan içerik dosyaYolu değişkenine kaydediliyor
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            dosyaYolu = br.readLine();

            //dosya kapatılıyor
            fr.close();
            br.close();

            //eğer dosya okuma işlemi başarısız olursa olacak olaylar
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        //gerekli bir değişken
        BufferedImage foto = null;
        try {

            //dosya yolu verilen dosyadaki fotoğrafı okuyup foto değişkenine kaydediyor
            foto = ImageIO.read(new File(dosyaYolu));

            //okuma sırasında hata olursa olacaklar
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        //fotonun genişlik ve yükseklik değerleri kaydediliyor
        int width = foto.getWidth();
        int height = foto.getHeight();

        // Belirli bir genişlik ve yükseklikte boş bir BufferedImage oluşturulur
        // Bu yeni görüntü, siyah-beyaz efekti uygulanacak fotoğrafın boyutlarını alır
        BufferedImage filtreliFoto = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(foto.getRGB(x, y));  // Her pikselin rengi alınır

                int red = color.getRed();  // Kırmızı bileşen alınır
                int green = color.getGreen();  // Yeşil bileşen alınır
                int blue = color.getBlue();  // Mavi bileşen alınır

                int gray = (red + green + blue) / 3;  // Gri tonlama formülü uygulanır

                Color newColor = new Color(gray, gray, gray);  // Gri renk oluşturulur
                filtreliFoto.setRGB(x, y, newColor.getRGB());  // Gri renk, belirtilen konuma yerleştirilir
            }
        }

        //dosya yolu ilk fotoğrafla aynı olan bir çıktı dosya nesnesi üretiliyor
        File ciktiDosya = new File(dosyaYolu);
        try {

            //filtreli fotoğrafı .jpg formatında çıktı dosyasa kaydediyor
            ImageIO.write(filtreliFoto, "jpg", ciktiDosya);

            //eğer hata olursa bunu ekrana yazarak belirtiyor
        } catch (IOException x) {
            System.err.println("Fotoğraf kaydedilirken bir hata oluştu: " + x.getMessage());
        }

        //Arayüzdeki fotoğraf için kullanılan label'ın içeriğini yeni filtreli fotoğrafımız ile değiştiriyor
        lblFoto.setIcon(new ImageIcon(filtreliFoto));
    }
    public void bulaniklastir(){

        //gerekli değişken
        String dosyaYolu;

        //temp.txt" dosyasının içindeki dosya konumunu okumak için nesne oluşturuluyor
        File f = new File("temp.txt");

        try {

            //dosya okumak için gerekli nesneler oluşturuluyor ve
            //okunan içerik dosyaYolu değişkenine kaydediliyor
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            dosyaYolu = br.readLine();

            //dosya kapatılıyor
            fr.close();
            br.close();

            //eğer dosya okuma işlemi başarısız olursa olacak olaylar
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        //gerekli değişken
        BufferedImage foto = null;
        try {

            //dosya yolu verilen dosyadaki fotoğrafı okuyup foto değişkenine kaydediyor
            foto = ImageIO.read(new File(dosyaYolu));

            //okuma sırasında hata olursa olacaklar
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        //fotonun genişlik ve yükseklik değerleri kaydediliyor
        int width = foto.getWidth();
        int height = foto.getHeight();


        // Belirli bir genişlik ve yükseklikte boş bir BufferedImage oluşturulur
        // Bu yeni görüntü, bulanıklaştırma efekti uygulanacak fotoğrafın boyutlarını alır
        BufferedImage filtreliFoto = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int radius = 5;  // Bulanıklaştırma yarıçapı belirlenir

        // Her piksel için bulanıklaştırma efekti uygulanır
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = getBulanikPiksel(foto, x, y, radius);  // Bulanıklaştırılmış piksel değeri alınır
                filtreliFoto.setRGB(x, y, rgb);  // Bulanıklaştırılmış piksel değeri belirlenen konuma yerleştirilir
            }
        }

        //dosya yolu ilk fotoğrafla aynı olan bir çıktı dosya nesnesi üretiliyor
        File outputFile = new File(dosyaYolu);
        try {

            //filtreli fotoğrafı .jpg formatında çıktı dosyasa kaydediyor
            ImageIO.write(filtreliFoto, "jpg", outputFile);

            // bir hata olursa ekrana hata mesajı yazdırılıyor
        } catch (IOException x) {
            System.err.println("Fotoğraf kaydedilirken bir hata oluştu: " + x.getMessage());
        }

        //Arayüzdeki fotoğraf için kullanılan label'ın içeriğini yeni filtreli fotoğrafımız ile değiştiriyor
        lblFoto.setIcon(new ImageIcon(filtreliFoto));
    }

    public int getBulanikPiksel(BufferedImage image, int x, int y, int radius) {
        int sumRed = 0;
        int sumGreen = 0;
        int sumBlue = 0;
        int count = 0;

        // Belirli bir yarıçap içindeki piksellerin renk değerlerini toplama
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int pixelX = x + i;
                int pixelY = y + j;

                if (pixelX >= 0 && pixelX < image.getWidth() && pixelY >= 0 && pixelY < image.getHeight()) {
                    int rgb = image.getRGB(pixelX, pixelY);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    sumRed += red;
                    sumGreen += green;
                    sumBlue += blue;
                    count++;
                }
            }
        }

        // Ortalama renk değerlerini hesaplama
        int avgRed = sumRed / count;
        int avgGreen = sumGreen / count;
        int avgBlue = sumBlue / count;

        // Yeni pikselin RGB değerini oluşturma
        int newRGB = (avgRed << 16) | (avgGreen << 8) | avgBlue;

        return newRGB;
    }

}

class Twitter {

    //gerekli değişkenler
    private final String kullaniciAdi;
    private final String sifre;
    private final WebDriver driver;

    public Twitter(String kullaniciAdi, String sifre) {
        //kullaniciAdi isimli değişken kullaniciAdi içerisindeki değeri alıyor
        this.kullaniciAdi = kullaniciAdi;

        //sifre isimli değişken sifre içindeki değişkeni alıyor
        this.sifre = sifre;

        //driver olarak ChromeDriver kullanılıyor
        //bu sayede Google Chrome tarayıcıda işlemleri yapabiliyor
        this.driver = new ChromeDriver();
    }

    public void girisYap() {

        //twitter'ın giriş ekranına giriyor
        driver.get("https://twitter.com/login");

        //gerekli bilgileri otomatik doldurmak için gerekli
        By selectorUsername = By.cssSelector("[autocomplete='username']");
        By selectorPassword = By.cssSelector("[autocomplete='current-password']");
        By selectorUsernameNext = By.xpath("//span[text()='İleri']");
        By selectorLogin = By.xpath("//span[text()='Giriş yap']");

        //sayfanın açılması bekleniyor
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.presenceOfElementLocated(selectorUsername));
        //elementler bulunuyor ve keyler gönderiliyor
        driver.findElement(selectorUsername).sendKeys(this.kullaniciAdi);
        //İleri tuşuna tıklıyor
        driver.findElement(selectorUsernameNext).click();
        //şifre girme sayfasının açılması bekleniyor
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.presenceOfElementLocated(selectorPassword));
        //şifre elementleri bulunuyor ve keyler gönderiliyor
        driver.findElement(selectorPassword).sendKeys(this.sifre);
        //giriş yap tuşuna tıklanıyor
        driver.findElement(selectorLogin).click();
        //anasayfanın açılması bekleniyor
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.urlMatches("https://twitter.com/home"));
    }

    public void fotoPaylas(String imagePath) {
        driver.get("https://twitter.com/compose/tweet");

        By selectorMediaInput = By.xpath("//input[@accept='image/jpeg,image/png,image/webp,image/gif,video/mp4,video/quicktime']");
        By selectorTweet = By.xpath("//span[contains(text(), 'Tweet')]");

        //sayfanın yüklenmesi bekleniyor
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.presenceOfElementLocated(selectorTweet));

        //fotoğraf seçiliyor ve paylaşılıyor
        driver.findElement(selectorMediaInput).sendKeys(imagePath);
        driver.findElement(selectorTweet).click();
    }

    //tarayıcı kapatılıyor
    public void quit() {
        driver.quit();
    }
}

class paylasGUI extends JFrame {

    //gerekli değişkenler
    JPanel panel;
    JButton btnTwitter;
    private JTextField txtKullanici, txtSifre;

    public paylasGUI() {

        //twitter'da paylaş butonuna basıldığını kontrol etmek için bir action listener ekleniyor
        btnTwitter.addActionListener(new ActionListener() {
            @Override

            //eğer butona basılırsa aşağıdaki şeyler olacak
            public void actionPerformed(ActionEvent e) {

                //"temp.txt" dosyası için bir File değişkeni oluşturuluyor
                File file = new File("temp.txt");

                //dosya okumak için gerekli nesneler oluşturuluyor
                FileReader fr = null;
                try {
                    fr = new FileReader(file);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                BufferedReader br = new BufferedReader(fr);

                //gerekli değişken
                String dosyaYolu = null;

                try {

                    //okunan dosya yolu dosyaYolu isimli değişkene kaydediliyor
                    dosyaYolu = br.readLine();

                    //hata alınması durumunda yapılacaklar
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                //yeni bir twitter nesnesi oluşturuluyor ve bu nesneyi
                //oluştururken constructor'ın aldığı parametreler arayüzden okunuypr
                Twitter twitter = new Twitter(txtKullanici.getText(), txtSifre.getText());

                //twitter class'ındaki girişYap ve fotoPaylas metodları çağırılıyor
                twitter.girisYap();
                twitter.fotoPaylas(dosyaYolu);
            }
        });
    }
}