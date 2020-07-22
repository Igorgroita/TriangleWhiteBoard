import java.awt.*;
import java.applet.*;
import java.util.List;
import java.util.ArrayList;

public class Board extends Applet{
	List <Point> locGeometric = new ArrayList<Point>(); //pastrez punctele prin care trec centrele 
	Button restart, mediane, inaltimi, bisectoare; 
	Point[] P;  // cele trei puncte care formeaza triunghiul
	Point[] M;  // punctele cu ajutorul carora formez medianele
	Point[] H;  // punctele cu ajutorul carora formez inaltimile
	Point[] B;  // punctele cu ajutorul carora formez bisectoarele
	Point pG, pH, pI, O; // Centrul de greutate, Ortocentru, Centrul cercului inscris, Centrul cercului circumscris
	Image im;
	Graphics img;
	int nrpcte, moveflag=100000;
	boolean dMediane = false, dInaltimi = false, dBisectoare = false, changeRadius; //pentru activarea si dezactivarea butoanelor
	double p, S, R, alpha, beta; //semiperimetru triunghiului, aria triunghiului respectiv raza cercului circumscris
	double[] l;	//lungimile laturilor triunghiului
	public void init(){
		im = createImage(size().width, size().height);
		img = im.getGraphics();
		
		restart = new Button("Restart");
		inaltimi = new Button("Inaltimi");
		mediane = new Button("Mediane");
		bisectoare = new Button("Bisectoare");

		add(restart);
		add(mediane);
		add(inaltimi);
		add(bisectoare);
	
		mediane.setEnabled(false);
		inaltimi.setEnabled(false);
		bisectoare.setEnabled(false);
		nrpcte = 0;
		changeRadius = true;
		P = new Point[3];
		M = new Point[3];
		H = new Point[3];
		B = new Point[3];
		l = new double[3];	
		
	}
	
	public void update(Graphics g){
		paint(g);
	}
	
	
	
	public void deseneazaPunct(Point p, Color col){
		img.setColor(col); img.fillOval(p.x-3, p.y-3,6,6);
		img.setColor(Color.black); img.drawOval(p.x-3,p.y-3,6,6);
	}
	public void deseneazaLinie(Point A, Point B, Color col){
		img.setColor(col);
		img.drawLine(A.x,A.y,B.x,B.y);
		
	}
	
	//pentru trasarea traiectoriei
	public void traseazaVerde(Point A, Point B){
		img.setColor(Color.green);
		img.drawLine(A.x,A.y,B.x,B.y);
		img.drawLine(A.x-1,A.y-1,B.x-1,B.y-1);
		img.drawLine(A.x+1,A.y+1,B.x+1,B.y+1);
		img.drawLine(A.x-1,A.y-1,B.x+1,B.y+1);
		img.drawLine(A.x+1,A.y+1,B.x-1,B.y-1);
		
	}
	//distanta intre doua puncte
	double dist(Point A, Point B){
		double d = Math.sqrt((double)((A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y)));
		return d;
	}
	
	
	//Daca inaltimile sunt in afara triunghiului le prelungesc cu linii intrerupte.
	public void deseneazaLinieIntrerupta(Point A, Point B, Color col){     
		double pas = 10.0/dist(A,B);
		
		for(double t = pas; t<1 - pas; t+=2*pas){
			double s = t+pas;
			Point p1 = new Point((int)((double)A.x*t+(double)B.x*(1-t)), (int)((double)A.y*t+(double)B.y*(1-t)));
			Point p2 = new Point((int)((double)A.x*s+(double)B.x*(1-s)), (int)((double)A.y*s+(double)B.y*(1-s)));
			deseneazaLinie(p1,p2,col);
		}
		
	}
	
	
	
	//Avem 3 necunoscute: coordonatele centrului si raza cercului circumscris.
	//Avem 3 ecuatii: cele 3 puncte din plan selectate satisfac ecuatia cercului cautat.
	
	public Point centruCircumscris(Point A, Point B,Point C){
		
    int x12 = A.x - B.x; int x21 = -x12;
    int x13 = A.x - C.x; int x31 = -x13;
  
    int y12 = A.y - B.y; int y21 = -y12; 
    int y13 = A.y - C.y; int y31 = -y13;   
  
    int sx13 = (int)(Math.pow(A.x, 2) - Math.pow(C.x, 2)); 
    int sy13 = (int)(Math.pow(A.y, 2) - Math.pow(C.y, 2)); 
    int sx21 = (int)(Math.pow(B.x, 2) - Math.pow(A.x, 2));               
    int sy21 = (int)(Math.pow(B.y, 2) - Math.pow(A.y, 2)); 
  
    int f = ((sx13) * (x12) + (sy13) * (x12) + (sx21) * (x13) + (sy21) * (x13)) / (2 * ((y31) * (x12) - (y21) * (x13))); 
    int g = ((sx13) * (y12)  + (sy13) * (y12) + (sx21) * (y13) + (sy21) * (y13))/ (2 * ((x31) * (y12) - (x21) * (y13))); 
    int c = -(int)Math.pow(A.x, 2) - (int)Math.pow(A.y, 2) - 2 * g * A.x - 2 * f * A.y; 
  
    int h = -g; 
    int k = -f; 
 
    return new Point(h,k);
	}
	
	//Returneaza punct care este combinatie liniara a alte 2 puncte.
	public Point combLin(double t, Point A, Point B){
		return new Point((int)(t*(double)A.x + (1-t)*(double)B.x),(int)(t*(double)A.y + (1-t)*(double)B.y));
	}
	
	
	//Desenez cercul circumscris.
	void CercCircumscris(){
		l[0] = dist(P[2],P[1]);
		l[1] = dist(P[0],P[2]);
		l[2] = dist(P[1],P[0]);
		p = (l[0]+l[1]+l[2])/2;
		S = Math.sqrt(p*(p-l[0])*(p-l[1])*(p-l[2]));
		if(changeRadius)	R = l[0]*l[1]*l[2]/4/S;
		
		//Centrul ramane neschimbat odata ce s-au ales 3 puncte distincte din plan
		if(O==null) O = centruCircumscris(P[0],P[1],P[2]);
		img.setColor(Color.blue);
		img.drawOval((int)((double)O.x-R+1),(int)((double)O.y-R+1),2*(int)R,2*(int)R);
		
		}
	
	
	
	public void paint(Graphics g){
		setBackground(Color.white);
		img.setColor(Color.black);
		img.clearRect(0,0,size().width,size().height);
		
		img.setColor(Color.black);
		for(int j = 0; j<nrpcte-1; j++){
			deseneazaLinie(P[j],P[j+1],Color.black);
		}
		if(nrpcte==3 && !P[0].equals(P[1])&& !P[0].equals(P[2])&& !P[2].equals(P[1])){
			//Daca avem 3 puncte distincte in plan
			//trasez linia intre primul si al treilea punct,
			//desenez cercul determinat de acestea,
			//activez butoanele.
			deseneazaLinie(P[0],P[nrpcte-1],Color.black);
			CercCircumscris();
			inaltimi.setEnabled(true);
			mediane.setEnabled(true);
			bisectoare.setEnabled(true);
			
			if(dMediane){
				for(int k = 0; k<nrpcte; ++k){
					//Medianele impart laturile in jumatate.
					M[k] = combLin(0.5,P[(k+1)%nrpcte],P[(k+2)%nrpcte]);
					deseneazaLinie(P[k],M[k],Color.gray);
					Color galbenInchis = new Color(150,150,0);
					deseneazaPunct(M[k],galbenInchis);
				}
				pG = combLin(0.333333,P[0],M[0]);
				deseneazaPunct(pG,Color.red);
				img.drawString("G", pG.x+9,pG.y-9);

			}
			else if(dInaltimi){
				Color galbenInchis = new Color(150,150,0);
				
				//Determinarea inaltimile
				//Punctul de pe latura opusa unui varf cu care se formeaza inaltimea
				//este intersectia a doua drepte perpendiculare din plan, care se determina usor.


				
				double xA = (double) P[0].x; double yA = (double) P[0].y;
				double xB = (double) P[1].x; double yB = (double) P[1].y;
				double xC = (double) P[2].x; double yC = (double) P[2].y;
				double aa = yC-yB, bb = xB-xC, cc, dd;
				cc = -yC*bb - xC*aa;
				dd = xA*bb - yA*aa;
				double xx = (bb*dd-aa*cc)/(aa*aa+bb*bb);
				double yy = (-aa*dd-bb*cc)/(aa*aa+bb*bb);
				H[0] = new Point((int)xx,(int)yy);
				deseneazaLinie(P[0],H[0],Color.gray);

				deseneazaPunct(H[0],galbenInchis);
				
				xA = (double) P[1].x; yA = (double) P[1].y;
				xB = (double) P[0].x; yB = (double) P[0].y;
				xC = (double) P[2].x; yC = (double) P[2].y;
				aa = yC-yB; bb = xB-xC;
				cc = -yC*bb - xC*aa;
				dd = xA*bb - yA*aa;
				xx = (bb*dd-aa*cc)/(aa*aa+bb*bb);
				yy = (-aa*dd-bb*cc)/(aa*aa+bb*bb);
				H[1] = new Point((int)xx,(int)yy);
				deseneazaLinie(P[1],H[1],Color.gray);

				deseneazaPunct(H[1],galbenInchis);
				
				xA = (double) P[2].x; yA = (double) P[2].y;
				xB = (double) P[1].x; yB = (double) P[1].y;
				xC = (double) P[0].x; yC = (double) P[0].y;
				aa = yC-yB; bb = xB-xC;
				cc = -yC*bb - xC*aa;
				dd = xA*bb - yA*aa;
				xx = (bb*dd-aa*cc)/(aa*aa+bb*bb);
				yy = (-aa*dd-bb*cc)/(aa*aa+bb*bb);
				H[2] = new Point((int)xx,(int)yy);
				deseneazaLinie(P[2],H[2],Color.gray);
				
				deseneazaPunct(H[2],galbenInchis);
				double pHxUp = (double)((xB*(xA-xC)+yB*(yA-yC))*(yC-yB) - (yC-yA)*(xA*(xB-xC)+yA*(yB-yC)));
				double pHxDown = (double)((xC-xB)*(yC-yA) - (yC-yB)*(xC-xA));
				double pHyUp = (double)((xB*(xA-xC)+yB*(yA-yC))*(xC-xB) - (xC-xA)*(xA*(xB-xC)+yA*(yB-yC)));
				double pHyDown = (double)((yC-yB)*(xC-xA) - (xC-xB)*(yC-yA));
				
				double pHx = pHxUp / pHxDown;
				double pHy = pHyUp / pHyDown;
				pH = new Point((int)pHx,(int)pHy);
				
				if(dist(O,pH)>R){
					deseneazaLinieIntrerupta(H[2],P[1], Color.black);
					deseneazaLinieIntrerupta(H[1],P[2],Color.black);
					deseneazaLinieIntrerupta(H[0],P[1],Color.black);
					deseneazaLinieIntrerupta(H[0],pH,Color.black);
					deseneazaLinieIntrerupta(H[1],pH,Color.black);
					deseneazaLinieIntrerupta(H[2],pH,Color.black);
				}
				deseneazaPunct(pH,Color.red);
				img.drawString("H", pH.x+9,pH.y-9);
			}
			else if(dBisectoare){
				//Am folosit Teorema bisectoarei la calcule
				for(int k = 0; k<nrpcte; k++){
					B[k] = combLin(l[(k+1)%nrpcte]/(l[(k+1)%nrpcte]+l[(k+2)%nrpcte]),P[(k+1)%nrpcte],P[(k+2)%nrpcte]);
					deseneazaLinie(P[k],B[k],Color.gray);
					Color galbenInchis = new Color(150,150,0);
					deseneazaPunct(B[k],galbenInchis);
				}
				double imp = 1/(l[0]+l[1]+l[2]);
				pI = new Point((int)(l[0]*imp*(double)P[0].x+l[1]*imp*(double)P[1].x+l[2]*imp*(double)P[2].x),(int)(l[0]*imp*(double)P[0].y+l[1]*imp*(double)P[1].y+l[2]*imp*(double)P[2].y));
				deseneazaPunct(pI,Color.red);
				double r = S/p;
				img.setColor(Color.blue);
				img.drawOval(pI.x-(int)r, pI.y-(int)r,2*(int)r,2*(int)r);
				img.drawString("I", pI.x+9,pI.y-9);
			}
			//Trasez locul geometric descris de centre. 
			for(int i =0; i<locGeometric.size()-1; i++){
				traseazaVerde(locGeometric.get(i),locGeometric.get(i+1));
			}
		}
		for(int j = 0; j<nrpcte; j++){
			deseneazaPunct(P[j],Color.yellow);
			img.drawString(""+(char)(65+j),P[j].x+9,P[j].y-9);
		}
		g.drawImage(im,0,0,this);
	}
	
	public boolean action(Event e, Object o){
		if(e.target==restart){
			nrpcte = 0; repaint();
			O = null;
			changeRadius = true;
			inaltimi.setEnabled(false);
			mediane.setEnabled(false);
			bisectoare.setEnabled(false);
			dMediane = false;
			dBisectoare = false;
			dInaltimi = false;
			return true;
		}
		else if(e.target==mediane){
			dMediane = true;
			dBisectoare = false;
			dInaltimi = false;
			repaint();
			return true;
		}
		else if(e.target==inaltimi){
			dMediane = false;
			dBisectoare = false;
			dInaltimi =  true;
			repaint();
			return true;
		}
		else if(e.target==bisectoare){
			dMediane = false;
			dBisectoare = true;
			dInaltimi = false;
			repaint();
			return true;
		}
		return false;
	}
	
	
	public boolean mouseDown(Event evt, int x, int y){
		Point pct = new Point(x,y);
		if(nrpcte<3){
			P[nrpcte++] = pct;
			repaint();
		}
		else{
			int imini = 0;
			double dmini = 10000000.0;
			//Caut cel mai apropiat punct de locul unde s-a intamplat click-ul.
			for(int k = 0; k<nrpcte; k++){
				double dd = dist(pct,P[k]);
				if(dd<dmini){
					dmini = dd;
					imini = k;
				}
			}
			//Daca sunt prea aproape de acel punct => il selectez.
			if(dist(pct,P[imini])<=7.0){
				moveflag = imini;
			}
		}
		return true;
	}
	//Folosesc proiectia pe cerc atunci cand vreau sa deplasez un punct de-a lungul cercului.
	public Point proiectiePeCerc(Point pct){
		double distLaCentru = dist(O,pct);
		double alpha = R/distLaCentru;
		double xx = (double)O.x+alpha*(double)(pct.x-O.x);
		double yy = (double)O.y+alpha*(double)(pct.y-O.y);
		
		Point Q = new Point((int)xx,(int)yy);
		return Q;
	}

	public boolean apartineCercului(Point pct){
		return Math.abs(dist(O,pct) - R)<0.05;
	}
	
	public boolean mouseDrag(Event evt, int x, int y){
		if(nrpcte==3){
		double xx, yy;

		if(moveflag<nrpcte){
			changeRadius = false;
			Point Q = proiectiePeCerc(new Point(x,y));
			// Din cauza conversiei de la int la double,
			// iar dupa efectuarea calculelor => inapoi
			// de la double la int
			// apar erori de calcul
			// Am incercat sa le inlatur cautand un punct mai potrivit in
			// jurul proiectiei pe cerc.
			for(int i = Q.x - 7; i<Q.x+8; ++i){
				for(int j = Q.y - 7; j<Q.y+8; ++j){
					if(Math.abs(dist(O,new Point(i,j))-R)<0.05){
						P[moveflag].move(i,j);
						break;
					}
				}
			}
			
		repaint();
		}
		// In cazul in care nu este selectat nici un punct verific daca
		// e selectez cercul.
		else if(moveflag>nrpcte){
			// Apare o problema cand scalez cercul de prea multe ori
			// Din cauza erorilor cauzate cand convertesc de la double la int
			// punctele parasesc putin conturul cercului, dar revin inapoi pe cerc daca sunt deplasate de-a lungul cercului.
			
			
			// Raza cercului se schimba doar in cazul in care cercul este scalat
			changeRadius = true;
			double r = dist(new Point(x,y),O);
			if(Math.abs(R-r)<10){
				alpha = r/R;
				for(int i = 0; i<nrpcte; ++i){
					//Scalez coordonatele fiecarui punct fata de centrul cercului cu raportul dinre raza noua si cea veche.
					xx = (double)O.x+alpha*(double)(P[i].x-O.x);
					yy = (double)O.y+alpha*(double)(P[i].y-O.y);
									
					P[i].move((int)(xx+.5),(int)(yy+.5));
				}
				repaint();
			}
		}
		//Daca unul dintre butoane este activ, si gasesc un punct care nu
		//este in lista, il adaug.
		if(dMediane) locGeometric.add(pG);
		else if(dInaltimi) locGeometric.add(pH);
		else if(dBisectoare) locGeometric.add(pI);

		}
	
		return true;
	}
		  
	public boolean mouseUp(Event evt, int x, int y){
		locGeometric.clear();
		moveflag = 1000;
		return true;
	}
}