
//	Nhan Vu


import java.util.*; 
import java.io.*;

public class Distance_Vector_Routing{

	//variables declarations

	//number of nodes
	static int n;

	//to mark non-adjacent edges
	static int invalid = -1;

	//node number
	static ArrayList<Integer> nodes;

	//infinity set to 16 for this hw assignment
	static int infinity = 16;

	//this graph tracks the shortest path from one node to another
	static int RouteTable[][];
	//this is a series of graph
	//there is one 2d graph for each node
	//each node graph list cost to all other node via another node path
	//ex: node NodeGraph[5][1][2] is distance from node 5 to 1 using a known path with 2
	static int NodeGraph[][][];
	//use first to update the 2nd Node graph, then make NodeGraph the same as the next
	static int NextNodeGraph[][][];
	//no new path way are introduced when set to false
	static boolean NeedsUpdate = true;
	//determine if there's a need to keep asking for input
	static boolean ask = true;
	//track how many iteration of updates
	static int update = 1;


	// main program

	public static void main(String[] args)throws Exception{
		//tracks what user whats to do next
		String option = "";

		//retrieve input file name from user
		String inputFile = getString("Enter name of input file: ");
		File file = new File(inputFile);

		//extracting infomation from file
		int input[] = ExtractNum(file); //using input file, extract the number 
		getNodes(input);			//fill Node with nodes from given info

		//intialzing input
		intiateTable(input);
		System.out.println("\nInitial Tables: \n");
		updateRouteTable();
		printNodeGraph();
		printRouteTable();
		
		while(NeedsUpdate){

			if(ask){
				System.out.println("Type 'next' to run algorithm once more");
				System.out.println("Type 'finish' to run until stable state");
				System.out.println("Type 'change' to change link cost");
				System.out.println("Type 'exit' to exit the program");
				
				option = getString("Enter your choice: ");
			}

			if(option.equals("next")){
				if(!NeedsUpdate){
					break;
				}
			}else if(option.equals("finish")){
				//continues and skip all next ask
				ask = false;
			}else if(option.equals("change")){
				System.out.println("Enter nodes seperated by a space, then another space, followed by new cost.");
				System.out.println("Example: 2 4 11");
				String modification = getString("");
				//check to see if input matches format
				if(modification.matches("\\d+ \\d+ \\d+")){
					//add new edge value onto previous input result and initiate table again
					changeEdge(modification, input);
				}else{
					System.out.println("Error! Link could not be made. Check your format. Exiting...");
				}
			}else if(option.equals("exit")){
				System.out.println("exiting...");
				break;
			}else{
				System.out.println("Your input was invalid. Exiting program...");
				break;
			}

			updateNodes();
			updateRouteTable();
			if(NeedsUpdate){
				System.out.printf("\nUpdated Tables for broadcast number %d:\n", update);
				printNodeGraph();
				printRouteTable();
			}
			update++;

			if(!NeedsUpdate){
				System.out.println("\nThe above graph is the final Distance Vector Graph with shortest Routes");
			}
		}
	
	}

	// end of main program

	//functions
//----------------------------------------------------------------------------------

	//print a request and then retrieve an answer as a string
	static String getString(String request)throws Exception{
		Scanner scanner = new Scanner(System.in);
  		System.out.print(request);
  		return scanner.nextLine();
	} 

	//this function extract the lines with 3 numbers and put it into an arrayList
	static int[] ExtractNum(File file)throws Exception{
		String line, num_str = "";
		BufferedReader buffer = new BufferedReader(new FileReader(file));
		while ((line = buffer.readLine()) != null){
			//add (.*) to accept anything either b4 or after
			//only add each string that follow the format # # #
			if(line.matches("\\d+ \\d+ \\d+")){
				num_str += line+" ";
			}else{
				//does nothing
				//ignore file input that does not follow format
			}	
		}
		//debugger print
		//System.out.println("num_str: "+num_str);
		
		String[] numbers = num_str.split(" ");
		int[] result = new int[numbers.length];
		for(int i = 0; i < numbers.length; i++){
			result[i] = Integer.parseInt(numbers[i]);
		} 
		return result;
	}

	//function changes edge value by simply adding it to the end of 
	static void changeEdge(String modification, int[] input)throws Exception{
		//turn modification string to int[]
		String[] num_str = modification.split(" ");
		int[] numbers = new int[num_str.length];
		for(int i = 0; i < num_str.length; i++){
			numbers[i] = Integer.parseInt(num_str[i]);
		}
		//add new numbers to input
		int[] new_input = new int[input.length + numbers.length];
		System.arraycopy(input, 0, new_input, 0, input.length);
		System.arraycopy(numbers, 0, new_input, input.length, numbers.length);

		//reinitate the table with new input
		for(int i = 0; i < n; i++){
			System.out.printf("%d ", nodes.get(i));
		}

		getNodes(new_input);
		intiateTable(new_input);
	}

	//fill Node with nodes from given info	
	static void getNodes(int[] num)throws Exception{
		int size = num.length;
		int temp;
		boolean unique = true;
		nodes = new ArrayList<Integer>();
		//add first unique
		nodes.add(num[0]);
		//add the rest only if unique
		for(int i = 1; i < size; i++){
			temp = num[i];
			//skip 3rd number since it is a cost and not a node
			if(((i+1)%3)!= 0){
				//check if node is unique
				for(int i2 = 0; i2 < nodes.size(); i2++){
					if(temp == nodes.get(i2)){
						unique = false;
					}
				}
				if(unique){
					nodes.add(temp);
				}
				unique = true;
			}		

		}
		//set n as size of node
		n = nodes.size();
		//optional bubble sort to make nodes orderly
		bubble(nodes);
	}

	//bubble sort function
	static void bubble(ArrayList<Integer> arr){
        for (int i = 0; i < n-1; i++)
            for (int j = 0; j < n-i-1; j++)
                if (arr.get(j) > arr.get(j+1)){
                    // swap temp and arr[i]
                    int temp = arr.get(j);
                    arr.set(j,arr.get(j+1));
                    arr.set(j+1,temp);
                }
    }


    static void intiateTable(int[] num)throws Exception{
    	//allocate tables
    	RouteTable = new int[n][n];
    	NodeGraph = new int[n][n-1][n-1];
    	NextNodeGraph = new int[n][n-1][n-1];
    	//set diagonal of Route Table to 0 and the rest to infinity
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				if(i == j){	//from any node to itself
					RouteTable[i][j] = 0;
				}else{
					RouteTable[i][j] = infinity;
				}
			}
		}
		//populate node tables
		int size = num.length;
		int[] temp = new int[3];

		System.out.println(n);
		//fill node tables with infinities
		for (int a = 0; a < n; a++){
			for (int b = 0; b < n-1; b++){
				for (int c = 0; c < n-1; c++){
					NodeGraph[a][b][c] = infinity;
					//dbugger print
					//System.out.printf("%d %d %d |\n", a,b,c);
				}
			}
		}

		for (int i = 0; i < size; i = i + 3){
			//store 3 number at a time and record them in nodes
			temp[0] = getIndex(num[i]);
			temp[1] = getIndex(num[i+1]);
			temp[2] = num[i+2];

			//dbugger print
			//System.out.printf("%d %d %d |\n", temp[0], temp[1], temp[2]);

			//adjust for index if the node comes after current node
			if(temp[0]<temp[1]){
				temp[1] = temp[1]-1;
			}
			//ex: temp = {3,2,6}
			//->it takes node graph 3 6 cost to get to 2 via 2
			//-1 since index start at 0
			//-1 again for to and via since the direction to current node isn't needed 
			NodeGraph[temp[0]][temp[1]][temp[1]] = temp[2];

			//undirected graph -> a to b is same cost as b to a
			//do the same thing but swap temp[1] and temp[2]
			temp[0] = getIndex(num[i+1]);
			temp[1] = getIndex(num[i]);
			temp[2] = num[i+2];
			if(temp[0]<temp[1]){
				temp[1] = temp[1]-1;
			}
			NodeGraph[temp[0]][temp[1]][temp[1]] = temp[2];
		}

		//mark any via column with no adjacent edge as invalid in the update
		for (int a = 0; a < n; a++){
			for (int b = 0; b < n-1; b++){
				if(NodeGraph[a][b][b] == infinity){
					for (int c = 0; c < n-1; c++){
						NodeGraph[a][c][b] = invalid;
						//dbugger print
						//System.out.printf("%d %d %d |\n", a,b,c);
					}	
				}
			}
		}

	}

	//this func gets the index of the node give the number
	static int getIndex(int node_num){
		//if func returns -1 then the node_num was not in nodes
		int result = -1;
		for (int i = 0; i < n; i++){
			if(node_num == nodes.get(i)){
				result = i;
			}
		}
		return result;
	}

	//pass shortest path to master routing table from all the nodes table
	static void updateRouteTable(){
		for(int a = 0; a < n; a++){
			for(int b = 0; b < n-1; b++){
				for(int c = 0; c < n-1; c++){
					int y = a;
					int x = b;
					if(x >= a){
						x++;
					}
					if((NodeGraph[a][b][c] < RouteTable[y][x]) && (NodeGraph[a][b][c] != invalid)){
						RouteTable[y][x] = 	NodeGraph[a][b][c];
						//dbugger print
						//System.out.printf("%d %d %d %d %d |", a,b,c,x,y);
					}
				}
			}
		}
	}

	//have each adjacent node share their current route 
	//update using Dx(y) = min{C(x,v) + Dv(y)}
	static void updateNodes(){
		copyNodeGraph();
		for(int a = 0; a < n; a++){
			for(int b = 0; b < n-1; b++){
				for(int c = 0; c < n-1; c++){				
					int x = adjustedUP(a, c);
					//adjustedDown will if both its arg are =
					//it will search for a nonexistent node x in the graph of x
					int y = adjustedDown(x, adjustedUP(a,b));
					//this if statement prevents arg of adjustedDown being = to be processed
					if(x != adjustedUP(a,b) && (NodeGraph[a][b][c] != invalid)){
						//dbugger print
						//System.out.printf("%d %d %d | %d %d |\n", a,b,c,x,y);
						//System.out.printf("%d %d |\n", RowMin(a,c), RowMin(x,y));

						//RowMin(a,c) is Dv(y) and RowMin(x,y) is (x,v)
						if((RowMin(a,c)+RowMin(x,y)) < NodeGraph[a][b][c]){
							NextNodeGraph[a][b][c] = RowMin(x,y)+RowMin(a,c);
							//System.out.printf("[%d]\n", RowMin(a,c)+RowMin(x,y));
						}
					}
				}
			}
		}
		copyNextNodeGraph();
	}

	//find minimum path cost from NodeGraph[from][to][via 0 to n-1]
	static int RowMin(int from, int to){
		int min = infinity;
		for(int i = 0; i < n-1; i++){
			if((NodeGraph[from][to][i] < min) && (NodeGraph[from][to][i] != invalid)){
				min = NodeGraph[from][to][i];
			}
		}
		return min;
	}

	//since RouteGraph skip current node in via and to, this realign position up
	//use for x coordinate of NodeGraph
	static int adjustedUP(int node, int position){
		if(position >= node){
			position++;
		}
		return position;
	}

	//since RouteGraph skip current node in via and to, this realign position down
	//use for y and z coordinate of NodeGraph
	//WARNING, you cannot do adjustedDown(i,i)
	//you can't find via or to i in node i
	static int adjustedDown(int node, int position){
		if(position > node){
			position--;
		}
		return position;
	}

	//copy NodeGraph onto NextNodeGraph
	static void copyNodeGraph(){
		for(int x = 0; x < n; x++){
			for(int y = 0; y < n-1; y++){
				for(int z = 0; z < n-1; z++){
					NextNodeGraph[x][y][z] = NodeGraph[x][y][z];
				}
			}
		}
	}

	//copy NextNodeGraph onto NodeGraph
	static void copyNextNodeGraph(){
		NeedsUpdate = false;
		for(int x = 0; x < n; x++){
			for(int y = 0; y < n-1; y++){
				for(int z = 0; z < n-1; z++){
					if(NodeGraph[x][y][z] != NextNodeGraph[x][y][z]){
						NeedsUpdate = true;
					}
					NodeGraph[x][y][z] = NextNodeGraph[x][y][z];
				}
			}
		}
	}

	//Gui functions
//---------------------------------------------------------------------------
	//printing final route graphs functions
	static void printRouteTable(){
		//formatting title
		System.out.println("\n     Distance Routing Vector Table");
		System.out.print("---------");
		for(int i = 0; i < n; i++){
			System.out.print("--------");
		}
		System.out.println();
		//end of title

		for(int x = 0; x <= n; x++){
			if(x == 0){
				System.out.print("        |");
			}else{
				System.out.printf(" To %-3d|", nodes.get(x-1));
			}
		}

		System.out.println();
		System.out.print("---------");
		for(int i = 0; i < n; i++){
			System.out.print("--------");
		}

		System.out.println();
		for(int y = 0; y < n; y++){
			System.out.printf("From %-3d|", nodes.get(y));
			for(int x = 0; x < n; x++){
				System.out.printf("%7d|", RouteTable[y][x]);
			}
			//newline
			System.out.println();
		}
		//formatting closing
		System.out.print("---------");
		for(int i = 0; i < n; i++){
			System.out.print("--------");
		}
		System.out.println();
		//end of graph
	}

	//print node graph
	static void printNodeGraph(){
		for (int node_num = 0; node_num < n; node_num++){

			//formatting title
			System.out.printf("\n           From Node %d\n", nodes.get(node_num));
			System.out.print("----------");
			for(int i = 0; i < n-1; i++){
				System.out.print("----------");
			}
			System.out.println();
			//end of title

			System.out.print("         |");
			for(int x = 0; x < n; x++){
				if(x == node_num){
					//do nothing
					//skip the current node in the graph
				}else{
					System.out.printf(" Via %-3d |", nodes.get(x));
				}
			}	System.out.println();

			System.out.print("----------");
			for(int i = 0; i < n-1; i++){
				System.out.print("----------");
			}
			System.out.println();

			for(int y = 0; y < n; y++){
				//again, skip the current node in its table
				if(y == node_num){
					//do nothing
					//skip the current node in the graph
				}else{
					System.out.printf("To %5d |", nodes.get(y));
					for(int x = 0; x < n; x++){
						if(x == node_num){
							//do nothing
							//skip the current node in the graph
						}else{
							int a = y;
							int b = x;
							//adjust index when passed the current node
							if(y > node_num){
								a = y-1;
							}
							if(x > node_num){
								b = x-1;
							}
							if(NodeGraph[node_num][a][b] == invalid){
								//if invalid the path is considered infinitiy
								System.out.printf(" %8d|", infinity);
							}else{
								System.out.printf(" %8d|", NodeGraph[node_num][a][b]);
							}
						}
					}
					System.out.println();
				}
			}
			System.out.print("----------");
			for(int i = 0; i < n-1; i++){
				System.out.print("----------");
			}
			System.out.printf("\n\n");
		}
	}
}
