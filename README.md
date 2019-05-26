# Pasientarmbånd med tilhørenede mobilapplikasjon

Dette fillageret er kildekode utviklet i sammenheng med et bachelorprosjekt innen Elektronikk ved UiA våren 2019 i samarbeid med konsulentselskapet Bouvet. Tittelen for prosjektet er "Pasientarmbånd med tilhørende mobilapplikasjon". Her ligger kildekode for en mobilapplikasjon (mso-login) og kildekode for et pasientarmbånd basert på en Nordic Semiconductor nRF52832-mikrokontroller (PasArm). 

## Mitt Sykehusopphold (mobilapp)

Kildekoden for mobilapplikasjonen (Mitt Sykehusopphold) ligger i mappen "mso-login". 
1. Det kreves at PC-en har Android Studio for å kunne åpne prosjektet. Android Studio lastes ned fra https://developer.android.com/studio/index.html
2. For å åpne prosjektet bør Android Studio være installert og åpnet på PC-en. 
3. I Android Studio, trykk på "File", "Open" og deretter lokaliser "mso-login"-mappen. Trykk deretter på "OK".
4. La prosjektet bygge en god stund. 

Prosjektet kan testes virtuelt eller på tilkoplet smartelefon via USB. Smartelefonen må ha operativssystemet Android (versjon 4.3 Jelly Bean og oppover). I tillegg må utviklermodus og "USB-debugging" være aktivert på smartelefonen. Se f.eks. http://www.lovemediasoft.com/article/no/enable-usb-debugging-on-android-device.html
1. Kople til smartelefon via USB. 
2. Trykk på den "Run 'app'" (den grønne pilen/trekanten øverst til høyre) eller trykk "Shift + F10" for å kjøre appen. 
3. Velg enten tilkoplet smartelefon eller bruk virtuell enhet i boksen som popper opp. 
4. Installer nødvendige SDK-er og programtillegg. Disse popper opp i bokser da Android Studio automatisk oppdager hvilke som mangler. 

Appen kan testes på mobiltelefon. Innloggingsystemet bruker CloudMQTT. Man kan logge på i offline-modus, eller logge inn med brukernavn og passord. Brukernavn og passord må ligge inne i CloudMQTT-systemet på forhånd om man vil bruke MQTT-tjenesten. Man kan logge inn med brukernavn "eksempel" og passord "eksempel". 

### PasArm (fastvare for mikrokontroller)

Kildekode for pasientarmbåndet (nRF52832-mikrokontroller) ligger inne i "PasArm"-mappen. 
1. Prosjektet kan åpnes i f.eks. SEGGER Embedded Studio (SES). 
2. SEGGER Embedded Studio bør være installert på PC-en. SES kan lastes ned fra https://www.segger.com/downloads/embedded-studio
3. Dobbelklikk filen "PasArm_pca10040_s132.emProject" i mappen "mso\PasArm\PasArm\pca10040\s132\ses" for å åpne prosjektet. '
4. For å teste prosjektet på utviklerkort (nRF52 DK) kan man i SES trykke "Build" i oppgavelinjen øverst og deretter "Build and Run".
