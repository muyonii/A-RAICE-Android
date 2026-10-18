package com.example.a_raiceandrd.data

import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.model.Disease
import com.example.a_raiceandrd.model.Treatment

object DiseaseRepo {

    private val diseases = listOf(
        Disease(
            1, "Bacterial Leaf Blight",
            "A serious bacterial disease of rice that causes seedlings to wilt and leaves to turn yellow, dry out and die. It thrives in warm, humid conditions and can be severe in susceptible varieties. Yield losses of 80-85% have been reported from early seedling infection (kresek) and about 30% from leaf blight.",
            "Bacterial",
            "First observed in 1884-85 in Fukuoka Prefecture (Kyushu), Japan, where it was first thought to be caused by acidic soils. The bacterium was isolated and named in 1911. It became widespread across other rice-growing regions of Asia from the 1960s and is now found in Asia, Africa, Australia, Latin America and the Caribbean.",
            "Caused by the bacterium Xanthomonas oryzae pv. oryzae. It survives between crops in rice stubble, straw and weed hosts, especially Leersia (cutgrass), and enters the plant through leaf pores (hydathodes), wounds and cracks at the base of the leaf sheaths. It spreads through splashing and wind-blown rain, irrigation and flood water, and leaf contact. Severity increases with flooding and heavy nitrogen fertilization, especially on susceptible varieties.",
            "Kresek (seedling phase): leaves wilt, roll up and turn grayish green, usually 1-3 weeks after transplanting, and the plant may die. Squeezing the base of an infected seedling releases yellowish bacterial ooze, and unlike stem borer damage, the plant does not pull out of the soil easily. Leaf blight (older plants): water-soaked to yellow-orange stripes start at leaf tips or edges and grow with wavy margins. Milky ooze droplets may appear on young lesions early in the morning. Lesions turn yellow to white, then grayish, and the leaf dries out.",
            "Plant resistant or tolerant varieties. Use clean seed from healthy plants or certified seed. Do not clip seedling leaf tips at transplanting. Keep fields free of weeds, especially Leersia. Destroy or plow under rice stubble, straw, ratoons and volunteer seedlings after harvest. Avoid excess nitrogen and apply it in balanced amounts. Keep good drainage and avoid prolonged flooding. Use proper plant spacing. Chemical control is largely ineffective, so prevention matters most.",
            listOf (
                Treatment("Biological control agents", "Beneficial bacteria such as Pseudomonas fluorescens can reduce the disease in studies, but their use is still limited and works best as part of an integrated approach, not on its own.", "Biological"),
                Treatment("Plant resistant varieties", "Growing varieties with resistance genes to the bacterium is the most common and most effective way to manage the disease, since chemical control has been largely ineffective. Ask your local agriculture office which resistant varieties suit your area.", "Cultural"),
                Treatment("Field sanitation and balanced fertilization", "After harvest, destroy or plow under rice stubble, straw, ratoons and volunteer seedlings, and remove weed hosts such as Leersia. Avoid excess nitrogen, keep good drainage, and do not clip seedling leaf tips at transplanting.", "Cultural")
        ),
            "IRRI Rice Knowledge Bank: Bacterial blight; PhilRice: Bacterial Leaf Blight fact sheet; EPPO Global Database: Xanthomonas oryzae pv. oryzae datasheet; CABI/Plantwise: Rice bacterial leaf blight (Pacific Pests and Pathogens fact sheet 418); Plantwise Nepal farmer factsheet (2013); Encyclopaedia Britannica: Rice bacterial blight.",
            R.drawable.bacterial_leaf_blight
        ),
        Disease (
            2, "Bacterial Leaf Streak",
            "A bacterial disease of rice that causes narrow, water-soaked streaks between the leaf veins, which later turn brown and dry out the leaves. It is common in hot, humid and rainy conditions and mostly damages plants during the vegetative stage. It is easily confused with bacterial leaf blight, but mature plants usually recover and lose little grain yield.",
            "Bacterial",
            "First observed in the Philippines in 1918, but at that time it was thought to be bacterial leaf blight. Later, scientists classified the two bacteria as two pathovars of the same species, Xanthomonas oryzae:pv. oryzae for leaf blight and pv. oryzicola for leaf streak.",
            "Caused by the bacterium Xanthomonas oryzae pv. oryzicola. It lives on leaves, in water and in crop debris left after harvest, and survives on infected residues, volunteer rice plants, weeds and wild rice. It spreads through wind, rain splash, irrigation water and infected seed. Frequent rainfall and high temperature and humidity favor the disease.",
            "Early symptoms are small, narrow, water-soaked lines between the leaf veins. The lesions look translucent when you hold the leaf up to the light. In humid conditions, yellow droplets of bacterial ooze that look like tiny beads may appear on the leaf surface. In severe cases the streaks turn brown and cover the whole leaf, which then looks like bacterial leaf blight. To tell them apart, blight stripes have wavy margins while leaf streak margins are straight. Narrow brown spot has thicker lesions that are not translucent and produce no ooze. A quick test: cut a leaf at the edge of a streak and place it in a glass of water. After about 5 minutes, bacterial ooze makes the water cloudy.",
            "Keep fields clean by removing weed hosts. Plow under rice stubble, straw, ratoons and volunteer seedlings after harvest. Use disease-free seed. In irrigated areas, avoid draining water from an infected field into another rice field. Check fields during rainy, humid weather, since that is when the disease spreads fastest.",
            listOf (
                Treatment ("Field Sanitation", "Remove weed hosts and plow under the rice stubble, straw, ratoons and volunteer seedlings after harvest. These carry the bacteria from one season to the next", "Cultural"),
                Treatment ("Use disease-free seed", "Infected seed can start the disease in the seedlings and carry it from one cropping season to the next. Use clean seed from healthy plants or a trusted source.", "Cultural"),
                Treatment("Manage irrigation water", "The bacteria spreads in irrigation water. In irrigated areas, do not drain water from an infected field into another rice field.", "Cultural")
        ),
            "IRRI Rice Knowledge Bank: Leaf streak; Plantwise (CABI) farmer factsheet: Bacterial Leaf Streak on Rice, Cambodia (2013, revised 2018); EPPO Global Database: Xanthomonas oryzae pv. oryzae datasheet; CABI/Pacific Pests, Pathogens, Weeds & Pesticides fact sheet 418.",
            R.drawable.bacterial_leaf_streak
        ),
        Disease (
            3, "Bakanae",
            "A fungal, seed-borne disease of rice that makes seedlings grow abnormally tall and thin, and often kills them. Plants that survive usually produce panicles with empty grains. The name comes from the Japanese words for \"foolish seedling\". It is widespread and has caused losses of up to 20% in outbreaks in South and Southeast Asia.",
            "Fungal",
            "The disease was first described in Japan, which is where its name comes from. It is now found in Africa, Asia, the Americas, Europe and Oceania. IRRI has reported losses in outbreaks of 20-50% in Japan, 15% in Thailand and 4% in India. In Fiji it is considered the most serious seed- and soil-borne disease of dryland rice.",
            "Caused by the fungus Fusarium fujikuroi (formerly Gibberella fujikuroi). It is mainly carried on or in infected seed, and seed-borne levels of up to 25% have been recorded. Spores also spread by wind and water, and soil infections cause root rot. The fungus also infects grass weeds and other crops such as maize, sorghum and sugarcane. It is favored by dryland cultivation and high temperatures above 30°C.",
            "Infected plants are several inches taller than healthy ones, thin, with yellowish-green leaves and pale green flag leaves. Many seedlings dry up and die in the seedbed or soon after transplanting, and infected seedlings may show lesions on the roots. Plants that survive have fewer tillers, and their panicles carry empty or partly filled grains. A pink-white fungal growth may appear at the base of the stem, and the lower nodes may turn pink to purple and grow roots.",
            "Use healthy, disease-free seed and do not save seed from fields that had bakanae. Treat seed before sowing. Rotate rice with non-susceptible crops such as vegetables or root crops after 2 years. Remove weeds and destroy infected plants, since the fungus also lives on grasses and spreads by spores.",
            listOf(
                Treatment("Seed treatment","Treating seed before sowing with fungicides such as thiram, captan or carbendazim at about 2 g per kg of seed is a recommended control. Ask your local agriculture office which products are approved in your area.", "Chemical"),
                Treatment("Use healthy seed", "Most infection starts from contaminated seed. Use clean seed from healthy plants or a trusted source, and avoid saving seed from fields where tall, pale seedlings appeared.", "Cultural"),
                Treatment("Crop rotation", "Rotate rice with non-susceptible crops such as vegetables or root crops after 2 years of rice, to reduce the fungus left in the field","Cultural")
            ),
            "CABI/Pacific Pests, Pathogens, Weeds & Pesticides fact sheet 429 (based on IRRI Rice Knowledge Bank and CABI Crop Protection Compendium); Tamil Nadu Agricultural University (TNAU) Agritech Portal: Bakanae disease / foot rot.",
            R.drawable.bakanae
        ),
        Disease(
            4, "Brown Spot",
            "A common fungal disease of rice that causes round brown spots on the leaves and on the grains. It is usually considered a minor disease compared with rice blast, and under natural conditions it causes about 4% grain yield loss on average, though losses of up to 34% have been recorded in parts of Africa and Asia. It is more damaging when plants are stressed, for example by poor nutrition or water supply",
            "Fungal",
            "Early reports of brown spot symptoms in rice go back to 1892 in Japan, and the fungus was formally described in the early 1900s. It is now found in rice-growing regions worldwide, and recent reviews describe it as a re-emerging disease.",
            "Caused by the fungus Bipolaris oryzae (also called Cochliobolus miyabeanus). It survives in infected seed, volunteer rice, rice debris and wild grasses. Seeds can carry the fungus at rates from under 1% up to 76%, so infected seed is a major way the disease starts each season.",
            "Seedlings show small, round, yellow-brown to brown spots. On older leaves the spots are brown with light brown to grey centers and a dark brown margin, often with a yellow halo around them. On the grains, brown spots with light centers (called \"eye spots\") appear and can discolor the seed. It can look like rice blast, but blast lesions are usually elongated and pointed at both ends, while brown spot lesions are rounder and brown with a yellow halo.",
            "Use healthy seed, since infected seed carries the fungus. Plant resistant or tolerant varieties when available. Apply fertilizer in balanced amounts and keep a steady water supply, because stressed plants get the disease more easily. Remove volunteer rice, crop debris and grass weeds, where the fungus survives between seasons.",
            listOf(
                Treatment("Seed treatment with fungicide", "IRRI recommends treating seed with fungicides such as iprodione, propiconazole, azoxystrobin, trifloxystrobin or carbendazim. Pre-soaking seed in cold water for 8 hours makes the treatment work better. Ask your local agriculture office which products are approved in your area.", "Chemical"),
                Treatment("Use healthy seed and resistant varieties", "Infected seed is a major source of the disease, and the fungus can survive in seed for more than 4 years. Plant clean seed from healthy plants and choose resistant or tolerant varieties if your area has them.", "Cultural"),
                Treatment("Balanced fertilizer and steady water supply", "Brown spot is common in nutrient-poor and unflooded soil, and improving soil fertility is the first step in managing it. Apply fertilizer at recommended rates and avoid letting plants dry out and become stressed.", "Cultural")
            ),
            "CABI/Pacific Pests, Pathogens, Weeds & Pesticides fact sheet 427 (based on IRRI Rice Knowledge Bank and CABI Crop Protection Compendium); Kaboré et al. (2025), Brown Spot of Rice: Worldwide Disease Impact, Phenotypic and Genetic Diversity of the Causal Pathogen Bipolaris oryzae, and Management of the Disease, Plant Pathology; IRRI Rice Knowledge Bank: Blast (Leaf and Collar), for telling blast and brown spot apart.",
            R.drawable.brown_spot
        ),
        Disease(
            5, "Grassy Stunt Virus",
            "A viral disease of rice spread by the brown planthopper. Infected plants are badly stunted with many thin tillers, which gives them a grassy, rosette look, and they usually produce no panicles. It is most common where rice is grown all year round, and it often occurs together with ragged stunt virus in the same field.",
            "Viral",
            "First reported in 1962-63 in Laguna, Philippines. Outbreaks followed in several Asian countries during the 1970s, including India, Indonesia, the Philippines and Thailand, a period when brown planthopper populations were rising.",
            "Caused by the rice grassy stunt virus (RGSV). It is carried from plant to plant by the brown planthopper (Nilaparvata lugens), which picks up the virus when it feeds on an infected plant and then spreads it to healthy ones. The disease is most common in areas where rice is grown continuously, because planthoppers always have rice to feed on.",
            "Plants are severely stunted with many tillers, so they look like clumps of grass. Leaves are short, narrow and pale green to yellow, often with rusty-brown spots. Infected plants usually produce no panicles.",
            "Plant brown planthopper-resistant varieties. Plant at the same time as neighboring fields, and avoid planting rice crops one right after another so planthoppers cannot move easily between them. Plow infected stubble into the soil after harvest. Avoid overusing insecticides, which kill the natural enemies of the planthopper and can make outbreaks worse. Check fields regularly for planthoppers.",
            listOf(
                Treatment("Plant planthopper-resistant varieties","Varieties that resist the brown planthopper reduce the spread of the virus, and IRRI lists them as a main control option. Ask your local agriculture office which varieties suit your area.", "Cultural"),
                Treatment("Synchronized planting", "Plant at the same time as neighboring farms, and avoid planting rice crops one right after another, so planthoppers cannot keep moving between young rice crops.", "Cultural"),
                Treatment("Plow under infected stubble","IRRI advises plowing stubble from infected fields into the soil after harvest, so it does not carry the virus over to the next crop.", "Cultural")
            ),
            "IRRI Rice Knowledge Bank: Grassy stunt; TNAU Agritech Portal: Grassy stunt; JIRCAS: Yield loss due to rice virus diseases (TARS 22) and technical bulletin on rice ragged stunt virus; Kusumaningrum et al., Jurnal Perlindungan Tanaman Indonesia (first report); CABI/Pacific Pests, Pathogens, Weeds & Pesticides fact sheet 064: Rice brown planthopper.",
            R.drawable.grassy_stunt_virus
        ),
        Disease(
            6, "Healthy Rice Plant",
            "No sign of disease was found on the scanned plant. ...",
            "", "", "", "",
            "Use clean, healthy seed. Apply fertilizer in balanced amounts and avoid excess nitrogen. Remove weeds and volunteer rice, and plow under stubble and straw after harvest. Check your fields regularly and scan again if you see spots, streaks, yellowing, stunting or unusual growth.",
            emptyList(),
            "General good practices drawn from the IRRI Rice Knowledge Bank and PhilRice pages listed for the other diseases.",
            R.drawable.healthy_rice_plant
        ),
        Disease(
            7, "Narrow Brown Spot",
            "A fungal disease of rice that causes short, narrow brown lines on the leaves, running along the veins. It usually shows up late in the season, as the crop approaches maturity. In severe cases, leaves die early, the grain ripens prematurely, plants may lodge (fall over) and yield drops. It is described as a re-emerging disease in some major rice-growing regions.",
            "Fungal",
            "",
            "Caused by the fungus Cercospora janseana (also known as Sphaerulina oryzina). It is favored by warm weather of about 25-28 degrees C and by potassium-poor soil. Louisiana research found the disease was worse in late-planted rice and when no nitrogen or too much nitrogen was applied. IRRI advises removing weeds and weedy rice from the field.",
            "Short, narrow, linear lesions, brown to reddish-brown, appear on the leaf blades running parallel to the veins. On the leaf sheaths the disease can form a net-like pattern, called net blotch. The lesions are thicker than those of bacterial leaf streak, are not translucent, and produce no bacterial ooze.",
            "Plant resistant or moderately resistant varieties recommended for your area. Plant earlier in the planting window, since late-planted rice had more disease in US trials. Split nitrogen into more than one application, and avoid applying none or too much. Remove weeds and weedy rice from the field and nearby areas.",
            listOf(
                Treatment("Propiconazole fungicide", "IRRI advises spraying propiconazole between booting and heading when a field is at risk, and a 2025 Louisiana guide gives the best timing as early boot to heading. Fungicide resistance can develop, so do not spray without need, and ask your local agriculture office for approved products and rates.", "Chemical"),
                Treatment("Plant resistant varieties", "In field trials, resistant varieties had the lowest disease, and the APS guide lists resistant or moderately resistant varieties as a main management step. Choose varieties recommended by your local agriculture office.", "Cultural"),
                Treatment("Earlier planting and split nitrogen", "In Louisiana trials, rice planted earlier in the season had less narrow brown spot, and splitting nitrogen into more than one application lowered severity on susceptible varieties compared with a single application. Both no nitrogen and too much nitrogen made the disease worse, so apply nitrogen at recommended rates.", "Cultural")
            ),
            "IRRI Rice Knowledge Bank: Narrow brown spot; LSU AgCenter research papers and 2025 Louisiana Plant Disease Management Guide; University of Arkansas Extension FSA2213: Cercospora diseases in rice; APS Education Center (2026): Narrow Brown Leaf Spot of Rice; Texas A&M Plant Disease Handbook: Rice; Plantwise (CABI) factsheet, Bacterial Leaf Streak on Rice, Cambodia (symptom comparison).",
            R.drawable.narrow_brown_spot
        ),
        Disease(
            8, "Ragged Stunt Virus",
            "A viral disease of rice spread by the brown planthopper. Infected plants are stunted with twisted, ragged leaves and swellings along the veins, and their panicles are often poorly filled. It cannot be cured once a plant is infected, so prevention is the main defense.",
            "Viral",
            "First observed in Indonesia in 1976, where it was called \"kerdil hampa\", and found in the Philippines in 1977. It was soon reported in Thailand and other Asian rice-growing countries. Damage increased as brown planthopper populations grew from the early 1970s onward.",
            "Caused by the rice ragged stunt virus (RRSV), which is carried only by the brown planthopper (Nilaparvata lugens), a pest that can migrate long distances. How much disease appears depends mainly on how many planthoppers are present, and young nymphs are more efficient at spreading the virus. Infected stubble and volunteer rice plants can serve as virus sources for the next crop. It often occurs together with grassy stunt virus.",
            "Plants are stunted and dark green. Leaf edges are ragged and twisted. Whitish, swollen outgrowths (vein swellings) appear on the leaves and sheaths. Panicles are only partly pushed out of the sheath and many grains are unfilled.",
            "Plant resistant varieties. Plant at the same time as neighboring fields and avoid planting rice crops one right after another. Plow under infected stubble and remove volunteer rice after harvest. Avoid overusing insecticides and protect natural enemies of the planthopper. Check fields regularly for planthoppers.",
            listOf(
                Treatment("Protect natural enemies of the planthopper", "Overuse of insecticides is the main cause of brown planthopper outbreaks, because it kills spiders, ladybird beetles, dragonflies and other natural enemies, and planthopper numbers then rebound higher than before. Spray only when planthopper numbers are likely to cause serious damage, and let flowering weeds grow on bunds and field borders to attract natural enemies.", "Biological"),
                Treatment("Plant resistant varieties", "IRRI considers resistant varieties the most important control: varieties resistant to the planthopper, to the virus, or both. There is no cure once a plant is infected.", "Cultural"),
                Treatment("Synchronized planting", "Plant at the same time as neighboring farms, and avoid planting rice crops one right after another, so large planthopper populations cannot easily move between fields.", "Cultural")
            ),
            "IRRI Rice Knowledge Bank: Ragged stunt; JIRCAS Journal (JARQ 17-2): Transmission of rice ragged stunt disease; JIRCAS technical bulletin: Rice ragged stunt virus; CABI/Pacific Pests, Pathogens, Weeds & Pesticides fact sheet 064: Rice brown planthopper.",
            R.drawable.ragged_stunt_virus
        ),
        Disease(
            9, "Rice Blast",
            "One of the most serious fungal diseases of rice, found in nearly every rice-growing region. It causes diamond-shaped spots on the leaves and can also attack the nodes and the neck of the panicle, which leads to empty grains. In severe cases it can kill seedlings. Losses are commonly estimated at 10-30% in epidemic years and can be much higher in severely infected fields.",
            "Fungal",
            "First described as \"rice fever\" in China in 1637, then in Japan in 1704 and in Italy in 1828. It is now found in about 85 countries. In tropical Asia it became more prominent after the Green Revolution began around 1960.",
            "Caused by the fungus Magnaporthe oryzae (also called Pyricularia oryzae), which spreads through airborne spores. IRRI lists these conditions as favorable: low soil moisture, long periods of rain or heavy dew, cool daytime temperatures and excess nitrogen fertilizer.",
            "Leaf blast starts as small spots that grow into diamond-shaped or spindle-shaped lesions with grey or white centers and dark borders. Blast at the collar, where the leaf meets the sheath, can kill the whole leaf. The fungus can also infect the nodes and the neck of the panicle, which can cause empty or partly filled grains.",
            "Plant resistant varieties. Avoid excess nitrogen. Plant at about the same time as neighboring farmers. For upland rice, treat seed with fungicide 1-2 days before sowing. Check fields often during long rainy periods and cool weather.",
            listOf(
                Treatment("Fungicide seed treatment", "For upland rice, treating seed with a fungicide 1-2 days before sowing is recommended to reduce infection. Fungicide sprays on the growing crop exist but are costly for small farms, so ask your local agriculture office whether they are worthwhile and which products are approved.", "Chemical"),
                Treatment("Plant resistant varieties", "IRRI names resistant varieties as the primary control for blast. Choose varieties recommended for your area, since different races of the fungus occur in different regions.", "Cultural"),
                Treatment("Balanced fertilizer and same-time planting", "Excess nitrogen makes blast worse, so apply fertilizer at recommended rates. Planting at about the same time as neighboring farmers is also recommended.", "Cultural")
            ),
            "IRRI Rice Knowledge Bank: Blast (Leaf and Collar); CABI/Pacific Pests, Pathogens, Weeds & Pesticides fact sheet 252: Rice blast; JIRCAS (2022): Dissemination and use of differential system against rice blast; Plant Archives: Blast disease of rice, a comprehensive review; Journal of Fungi (2022): Understanding the dynamics of blast resistance in rice-Magnaporthe oryzae interactions.",
            R.drawable.rice_blast
        ),
        Disease(
            10, "Rice False Smut",
            "A fungal disease that attacks the rice flowers and turns a few grains on a panicle into velvety balls. It is often minor, but outbreaks can cause serious losses, with up to 75% yield reduction reported. It is more common in rainy, cloudy weather during flowering.",
            "Fungal",
            "First documented in 1878 in Tirunelveli, Tamil Nadu, India. In India it was long called \"Lakshmi disease\" because the golden balls were seen as a sign of a good harvest, and it was considered a minor disease. It is now reported in most rice-growing countries and is a growing concern in many of them.",
            "Caused by the fungus Ustilaginoidea virens (sexual stage Villosiclava virens). It infects the rice flowers around booting and flowering. Rain and cloudy weather at flowering and high soil nitrogen favor it. The spores and hard resting bodies (sclerotia) can stay in the soil for several growing seasons and start new infections.",
            "Only a few grains per panicle are affected. Each affected grain becomes a velvety ball that is first covered by a whitish membrane, which bursts to show orange spores. The ball later turns yellowish-green and then dark green to black. The fungus also produces toxins (ustiloxins) that can contaminate the remaining grains.",
            "Use certified, disease-free seed. Destroy rice straw and stubble after harvest and keep irrigation channels and bunds clean. Avoid excess nitrogen. Check fields during rainy, cloudy weather around flowering.",
            listOf(
                Treatment("Preventive fungicide spray", "TNAU lists copper oxychloride and hexaconazole as sprays for false smut. Fungicides are used as a preventive measure, so timing matters. Ask your local agriculture office for approved products, doses and the right timing.", "Chemical"),
                Treatment("Use certified, disease-free seed", "Planting clean, certified seed lowers the amount of fungus brought into the field.", "Cultural"),
                Treatment("Field sanitation and balanced nitrogen", "Destroy straw and stubble after harvest, keep irrigation channels and bunds clean, and avoid excess nitrogen, which favors the disease.", "Cultural")
            ),
            "CABI/Pacific Pests, Pathogens, Weeds & Pesticides fact sheet 428: Rice false smut; TNAU Agritech Portal: False smut; Agricultural Reviews 44(1), 2023: false smut review; ICAR journal article: Ustilaginoidea virens isolates causing false smut disease of rice; Pestinfo Wiki: Ustilaginoidea virens.",
            R.drawable.rice_false_smut
        ),
        Disease(
            11, "Sheath Blight",
            "A fungal disease that causes oval, greenish-grey lesions on the leaf sheaths near the water line. The lesions spread upward and to neighboring plants, and in bad cases leaves dry out and young tillers die. It is worse in dense, heavily fertilized crops in warm, humid weather. No rice cultivar is fully resistant to it.",
            "Fungal",
            "",
            "Caused by the soil fungus Rhizoctonia solani, which survives between crops in the soil and in plant debris. The disease is favored by temperatures of 28-32 degrees C, very high humidity (85-100%) inside the crop canopy, dense planting and high nitrogen. It spreads upward through the plant and from one tiller to the next.",
            "Oval or irregular greenish-grey spots, about 1-3 cm long, appear on the leaf sheath near the water line. As the disease advances, the spots merge, move up the plant and reach the leaves, which dry out. Severe infection can destroy young tillers.",
            "Use nitrogen efficiently and do not over-fertilize. Avoid planting too densely and use wider spacing. Keep fields free of weeds and bury infected stubble by deep plowing after harvest. Drain the field for a few days at maximum tillering. Check the base of plants regularly in warm, humid weather.",
            listOf(
                Treatment("Biological control agents", "PalayCheck lists beneficial microbes that can suppress the fungus, including Trichoderma harzianum, Trichoderma viride, Bacillus subtilis, Bacillus cereus and Pseudomonas fluorescens. Use products registered in the Philippines and ask your local agriculture office for advice.", "Biological"),
                Treatment("Efficient nitrogen use and wider spacing", "High nitrogen and dense plants create the humid canopy the fungus likes. Apply nitrogen at recommended rates and use wider spacing or a lower seeding rate.","Cultural"),
                Treatment("Field sanitation and water management", "Remove weeds, plow deeply to bury infected stubble after harvest, and drain the field for a few days at maximum tillering, as advised by PalayCheck. IRRI also advises draining early in the season.", "Cultural")
            ),
            "IRRI Rice Knowledge Bank: Sheath blight; PalayCheck (Pinoy Rice Knowledge Bank): Sheath blight; APS Education Center: Rice sheath blight.",
            R.drawable.sheath_blight
        ),
        Disease(
            12, "Sheath Rot",
            "A fungal disease that rots the top leaf sheath, the one that wraps the young panicle. The panicle may stay inside the sheath or come out only partly, and the grains become discolored and poorly filled. It is more common in the wet season. Yield losses reported in studies vary widely, from about 20% to 85%.",
            "Fungal",
            "The fungus associated with the disease was first isolated in Taiwan in 1922.",
            "Caused by the fungus Sarocladium oryzae. It is seed-borne and survives on crop residue after harvest, so it can infect the following crop. It is more common in the wet season, in dense plantings and in plants wounded by insects.",
            "Irregular brown spots form on the uppermost leaf sheath, the one covering the young panicle. The panicle may stay inside the sheath or come out only partly, and it can rot. Grains are discolored and often poorly filled.",
            "Use healthy seed from uninfected plants. Control insects that wound the plants. Destroy crop residue after harvest. Avoid overly dense planting.",
            listOf(
                Treatment("Use healthy seed", "The fungus is carried on seed, so plant clean seed from healthy plants and avoid saving seed from fields that had sheath rot.", "Cultural"),
                Treatment("Control insects", "Insects wound the plants, which makes it easier for the fungus to infect them. IRRI advises minimizing insect infestation in the field.", "Cultural"),
                Treatment("Destroy crop residue", "The fungus survives on crop residue after harvest and can infect the next crop. Destroy residue from infected fields after harvest.", "Cultural")
            ),
            "IRRI Rice Knowledge Bank: Sheath rot; Frontiers in Plant Science (2015): Sarocladium oryzae review; Sri Lanka Department of Agriculture: Sheath rot.",
            R.drawable.sheath_rot
        ),
        Disease(
            13, "Stem Rot",
            "A fungal disease that causes black lesions on the leaf sheath near the water line late in the season, and then rots the stem. Infected plants can lodge (fall over), grains may be chalky or unfilled, and tillers can die.",
            "Fungal",
            "",
            "Caused by the fungus Magnaporthe salvinii (also called Sclerotium oryzae). It survives as small, hard black balls (sclerotia) in straw and stubble and spreads through water. Insect damage, too much nitrogen and too little potassium make the disease worse.",
            "Black lesions appear on the outer leaf sheath at water level around heading and grain filling. The fungus then moves into the stem and rots it, causing lodging, chalky grains, unfilled panicles and dead tillers.",
            "Apply fertilizer in balanced amounts, avoiding excess nitrogen and making sure plants get enough potassium. Control insect pests. Remove straw and stubble from infected fields after harvest.",
            listOf(
                Treatment("Balanced fertilizer, including potassium","Too much nitrogen and too little potassium increase stem rot. Apply fertilizer in balanced amounts, following the recommendations for your area.", "Cultural"),
                Treatment("Reduce insect damage","Insect damage makes stem rot worse, so controlling insect pests in the field helps reduce the disease.", "Cultural"),
                Treatment("Remove straw and stubble after harvest", "The fungus survives in straw and stubble as sclerotia, so removing them from infected fields after harvest reduces the source of infection for the next crop.", "Cultural")
            ),
            "CABI/Pacific Pests, Pathogens, Weeds & Pesticides fact sheet 430: Rice stem rot (based on IRRI and CABI Crop Protection Compendium); Plantix library: Stem rot of rice.",
            R.drawable.stem_rot
        ),
        Disease(
            14, "Tungro Virus",
            "A viral disease of rice and one of the most destructive in tropical Asia. Infected plants turn yellow to orange-yellow, become stunted and produce fewer tillers. It cannot be cured once a plant is infected, so prevention is much more effective than treatment. Plants are most vulnerable at the tillering stage.",
            "Viral",
            "First observed on the IRRI farm in the Philippines in 1963. Disease reports under local names such as \"penyakit merah\" in Malaysia and \"mentek\" in Indonesia were later shown to be the same disease. Rice virus diseases became increasingly important from the mid-1960s, and tungro spread widely as double-cropping with staggered planting expanded.",
            "Caused by two viruses together: rice tungro bacilliform virus (RTBV) and rice tungro spherical virus (RTSV). Leafhoppers, mainly the green leafhopper, pick up the viruses when they feed on infected plants and carry them to healthy ones. Staggered planting in areas with two rice crops a year is a major reason the disease became widespread after the 1960s.",
            "Leaves turn yellow to orange-yellow, and young leaves may look mottled. Plants are stunted and produce fewer tillers. Infection can happen at any growth stage, but plants are most vulnerable during tillering.",
            "Plant resistant varieties. Plant at the same time as neighboring farms and follow local advice on planting dates. Destroy rice stubble right after harvest. Where possible, plant a non-rice crop in the dry season. Avoid excess nitrogen. Insecticides alone often do not stop the disease.",
            listOf(
                Treatment("Plant resistant varieties", "IRRI lists resistant varieties as a main way to prevent tungro. Ask your local agriculture office or PhilRice for varieties suited to your area.", "Cultural"),
                Treatment("Synchronized planting and adjusted planting time", "Plant at the same time as neighboring farms and follow local advice on planting dates, because staggered planting is a major reason tungro spreads. Insecticides alone often do not stop the disease.", "Cultural"),
                Treatment("Destroy stubble and rotate crops", "Destroy rice stubble right after harvest and, where possible, plant a non-rice crop in the dry season to break the cycle of the disease.","Cultural")
            ),
            "IRRI Rice Knowledge Bank: Tungro and Green leafhopper; PhilRice: Diseases of the rice plant in the Philippines (flash cards); JIRCAS (TARS 22): Yield loss due to rice virus diseases; Indian Journal of Plant Genetic Resources article on resistance to tungro (first observation, 1963); APS Phytopathology (1983): K. C. Ling obituary (local disease names).",
            R.drawable.tungro_virus
        )
    )

    fun getAll(): List<Disease> = diseases

    fun getById(id: Int): Disease? = diseases.firstOrNull { it.id == id }
}