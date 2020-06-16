package me.renedo.service;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@Slf4j
class GeoServiceTest {

    final static String towns = "abajas,adrada de haza,aguas cándidas,aguilar de bureba,albillos,alcocero de mola,alfoz de bricia,alfoz de quintanadueñas,alfoz de "
            + "santa gadea,altable,los altos,altos,ameyugo,anguix,aranda de duero,arandilla,arauzo de miel,arauzo de salce,arauzo de "
            + "torre,arcos de la llana,arenillas de riopisuerga,arija,arlanzón,arraya de oca,atapuerca,los ausines,ausines,avellanosa "
            + "de muñó,bahabón de esgueva,los balbases,balbases,baños de valdearados,bañuelos de bureba,barbadillo de herreros,"
            + "barbadillo del mercado,barbadillo del pez,barrio de muñó,los barrios de bureba,barrios de bureba,barrios de colina,"
            + "basconcillos del tozo,bascuñana,belbimbre,belorado,berberana,berlangas de roa,berzosa de bureba,bozoó,brazacorta,"
            + "briviesca,bugedo,buniel,busto de bureba,cabañes de esgueva,cabezón de la sierra,caleruega,campillo de aranda,campolara,"
            + "canicosa de la sierra,cantabrana,carazo,carcedo de bureba,carcedo de burgos,cardeñadijo,cardeñajimeno,cardeñuela "
            + "riopico,carrias,cascajares de bureba,cascajares de la sierra,castellanos de castro,castil de peones,castildelgado,"
            + "castrillo de la reina,castrillo de la vega,castrillo de riopisuerga,castrillo del val,castrillo mota de judíos,"
            + "castrojeriz,cavia,cayuela,cebrecos,celada del camino,cerezo de río tirón,cerratón de juarros,ciadoncha,cillaperlata,"
            + "cilleruelo de abajo,cilleruelo de arriba,ciruelos de cervera,cogollos,condado de treviño,contreras,coruña del conde,"
            + "covarrubias,cubillo del campo,cubo de bureba,la cueva de roa,cuevas de san clemente,encío,espinosa de cervera,espinosa "
            + "de los monteros,espinosa del camino,estépar,fontioso,frandovínez,fresneda de la sierra tirón,fresneña,fresnillo de las "
            + "dueñas,fresno de río tirón,fresno de rodilla,frías,fuentebureba,fuentecén,fuentelcésped,fuentelisendo,fuentemolinos,"
            + "fuentenebro,fuentespina,galbarros,la gallega,grijalba,grisaleña,gumiel de izán,gumiel de mercado,hacinas,haza,hontanas,"
            + "hontangas,hontoria de la cantera,hontoria de valdearados,hontoria del pinar,las hormazas,hormanzas,hornillos del camino,"
            + "la horra,horra,hortigüela,hoyales de roa,huérmeces,huerta de arriba,huerta de rey,humada,hurones,ibeas de juarros,"
            + "ibrillos,iglesiarrubia,iglesias,isar,itero del castillo,jaramillo de la fuente,jaramillo quemado,junta de traslaloma,"
            + "junta de villalba de losa,jurisdicción de lara,jurisdicción de san zadornil,lerma,llano de bureba,madrigal del monte,"
            + "madrigalejo del monte,mahamud,mambrilla de castrejón,mambrillas de lara,mamolar,manciles,mazuela,mecerreyes,medina de "
            + "pomar,melgar de fernamental,merindad de cuesta-urria,merindad de montija,merindad de río ubierna,merindad de sotoscueva,"
            + "merindad de valdeporres,merindad de valdivielso,milagros,miranda de ebro,miraveche,modúbar de la emparedada,monasterio "
            + "de la sierra,monasterio de rodilla,moncalvillo,monterrubio de la demanda,montorio,moradillo de roa,nava de roa,navas de "
            + "bureba,nebreda,neila,olmedillo de roa,olmillos de muñó,oña,oquillas,orbaneja riopico,padilla de abajo,padilla de arriba,"
            + "padrones de bureba,palacios de la sierra,palacios de riopisuerga,palazuelos de la sierra,palazuelos de muñó,pampliega,"
            + "pancorbo,pardilla,partido de la sierra en tobalina,pedrosa de duero,pedrosa de río úrbel,pedrosa del páramo,pedrosa del "
            + "príncipe,peñaranda de duero,peral de arlanza,piérnigas,pineda de la sierra,pineda trasmonte,pinilla de los barruecos,"
            + "pinilla de los moros,pinilla trasmonte,poza de la sal,prádanos de bureba,pradoluengo,presencio,la puebla de arganzón,"
            + "puentedura,quemada,quintana del pidio,quintanabureba,quintanaélez,quintanaortuño,quintanapalla,quintanar de la sierra,"
            + "quintanavides,quintanilla de la mata,quintanilla del agua y tordueles,quintanilla del coco,quintanilla san garcía,"
            + "quintanilla vivar,las quintanillas,rabanera del pinar,rábanos,rabé de las calzadas,rebolledo de la torre,redecilla del "
            + "camino,redecilla del campo,regumiel de la sierra,reinoso,retuerta,revilla del campo,revilla vallejera,la revilla y "
            + "ahedo,revillarruz,rezmondo,riocavado de la sierra,roa,rojas,royuela de río franco,rubena,rublacedo de abajo,rucandio,"
            + "salas de bureba,salas de los infantes,saldaña de burgos,salinillas de bureba,san adrián de juarros,san juan del monte,"
            + "san mamés de burgos,san martín de rubiales,san millán de lara,san vicente del valle,santa cecilia,santa cruz de la "
            + "salceda,santa cruz del valle urbión,santa gadea del cid,santa inés,santa maría del campo,santa maría del invierno,santa "
            + "maría del mercadillo,santa maría rivarredonda,santa olalla de bureba,santibáñez de esgueva,santibáñez del val,santo "
            + "domingo de silos,sargentes de la lora,sarracín,sasamón,la sequera de haza,solarana,sordillos,sotillo de la ribera,"
            + "sotragero,sotresgudo,susinos del páramo,tamarón,tardajos,tejada,terradillos de esgueva,tinieblas de la sierra,tobar,"
            + "tordómar,torrecilla del monte,torregalindo,torrelara,torrepadre,torresandino,tórtoles de esgueva,tosantos,trespaderne,"
            + "tubilla del agua,tubilla del lago,úrbel del castillo,vadocondes,valdeande,valdezate,valdorros,vallarta de bureba,valle "
            + "de las navas,valle de losa,valle de manzanedo,valle de mena,valle de oca,valle de santibáñez,valle de sedano,valle de "
            + "tobalina,valle de valdebezana,valle de valdelaguna,valle de valdelucio,valle de zamanzas,vallejera,valles de palenzuela,"
            + "valluércanes,valmala,la vid de bureba,la vid y barrios,vileña,villadiego,villaescusa de roa,villaescusa la sombría,"
            + "villaespasa,villafranca montes de oca,villafruela,villagalijo,villagonzalo pedernales,villahoz,villalba de duero,"
            + "villalbilla de burgos,villalbilla de gumiel,villaldemiro,villalmanzo,villamayor de los montes,villamayor de treviño,"
            + "villambistia,villamedianilla,villamiel de la sierra,villangómez,villanueva de argaño,villanueva de carazo,villanueva de "
            + "gumiel,villanueva de teba,villaquirán de la puebla,villaquirán de los infantes,villarcayo de merindad de castilla la "
            + "vieja,villariezo,villasandino,villasur de herreros,villatuelda,villaverde del monte,villaverde-mogina,villayerno "
            + "morquillas,villazopeque,villegas,villoruebo,viloria de rioja,vilviestre del pinar,vizcaínos,zael,zarzosa de río "
            + "pisuerga,zazuar,zuñeda";

    @Inject
    GeoService geoService;

    @Test
    public void generateLocationsLatLon() {
        StringBuffer lat = new StringBuffer();
        StringBuffer lon = new StringBuffer();
        geoService.towns = towns.split(",");
        Arrays.asList(geoService.towns).stream().forEach(t -> {
            List<Pair<Double, Double>> latitudeLongitude = geoService.getLatitudeLongitude(t + ", Burgos, Spain");
            lat.append(",");
            lon.append(",");
            if (latitudeLongitude.size() >= 1) {
                lat.append(latitudeLongitude.get(0).getLeft());
                lon.append(latitudeLongitude.get(0).getRight());
            }else{
                lat.append(0);
                lon.append(0);
            }
        });
        log.info(lat.toString());
        log.info(lon.toString());
    }
}
