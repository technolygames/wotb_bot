package logic;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.awt.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.jfree.chart.plot.CategoryPlot;

/**
 * @author erick
 */
public class FileHandler{
    private final UtilityClass uc=new UtilityClass();

    public void chartApiMethodStats(String dir){
        DefaultCategoryDataset dataset=new DefaultCategoryDataset();
        try(Stream<Path> paths=Files.walk(Paths.get(dir))){
            paths.parallel()
                    .filter(Files::isRegularFile)
                    .filter(p->p.toString().endsWith(".csv"))
                    .forEach(p->{
                        Map<String,Map<String,List<Long>>> data=getCsvData(p.toString());
                        data.forEach((path,servers)->{
                            servers.forEach((server,id)->{
                                String filename=p.getFileName().toString().replace(".csv","");
                                for(Long val:id){
                                    dataset.addValue(val,server,path+"("+filename+")");
                                }
                            });
                        });
                    });
            
            createChartImage("Wg's API usage","Server","Requests",dataset);
        }catch(IOException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    public void createChartImage(String title,String legend,String lateral,DefaultCategoryDataset dataset) throws IOException{
        JFreeChart bar=ChartFactory.createBarChart(
                title,
                legend,
                lateral,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        bar.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot=bar.getCategoryPlot();
        plot.setBackgroundPaint(new Color(240,240,240));
        plot.setRangeGridlinePaint(Color.BLACK);

        File image=new File(exists(new File("data","reporte.png")));
        ChartUtils.saveChartAsPNG(image,bar,1920,1080);
    }
    
    public Map<String,Map<String,List<Long>>> getCsvData(String dir){
        Map<String,Map<String,List<Long>>> csvData=new HashMap<>();
        String currentPath="default_path";
        
        try(FileReader fr=new FileReader(dir);
                CSVReader cr=new CSVReaderBuilder(fr).build()){
            String[] line;
            while((line=cr.readNext())!=null){
                for(String cell:line){
                    String value=cell.trim();
                    if(!value.isEmpty()){
                        if(value.contains(";")){
                            String[] parts=value.split(";");
                            if(parts.length>1){
                                String server=parts[0].trim();
                                String num=parts[1].trim();
                                if(UtilityClass.isDigit(num)){
                                    csvData.computeIfAbsent(currentPath,k->new HashMap<>())
                                            .computeIfAbsent(server,k->new ArrayList<>())
                                            .add(Long.valueOf(num));
                                }
                            }
                        }else if(UtilityClass.isDigit(value)){
                            csvData.computeIfAbsent("general",k->new HashMap<>())
                                    .computeIfAbsent(currentPath,k->new ArrayList<>())
                                    .add(Long.valueOf(value));
                        }else{
                            currentPath=value;
                        }
                    }
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return csvData;
    }
    
    public static String exists(File file){
        String parent=file.getParent();

        File parent2=new File(parent);
        if(!parent2.exists()){
            parent2.mkdirs();
        }

        String filename=file.getName();

        String name=FilenameUtils.getBaseName(filename);
        String extension=FilenameUtils.getExtension(filename);
        String dot=(extension.isEmpty())?"":"."+extension;

        int i=1;
        File f2=new File(parent,name+dot);
        while(f2.exists()){
            f2=new File(parent,name+"-("+i+")"+dot);
            i++;
        }

        return f2.getPath();
    }
    
    /**
     * @return
     */
    public Map<String,String> getHelpCommandData(){
        Map<String,String> descriptions=new HashMap<>();
        Gson gson=new Gson();
        try(FileReader fr=new FileReader("data/command_data.json",StandardCharsets.UTF_8)){
            Type type=new TypeToken<Map<String,String>>(){}.getType();
            descriptions=gson.fromJson(fr,type);
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return descriptions;
    }
}