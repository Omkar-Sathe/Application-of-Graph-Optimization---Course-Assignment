# Application of Graph Optimization Course Assignment
# Project Description
The research problem at hand revolves around the optimization of the University Course Assignment System. Within a department, there are "n" faculty members categorized into three distinct groups: "x1," "x2," and "x3." Faculty in each category are assigned different course loads, with "x1" handling 0.5 courses per semester, "x2" taking 1 course per semester, and "x3" managing 1.5 courses per semester.

The primary objective is to develop an assignment scheme that maximizes the number of courses assigned to faculty while aligning with their preferences and the category-based constraints ("x1," "x2," "x3"). The challenge lies in ensuring that a course can only be assigned to a faculty member if it is present in their preference list.


# Installation
No installation is required. Clone the repository and run the code on a local device on any integrated development environment (IDE).

# Usage
To solve the problem at hand, execute the provided Java code. The CSV files contain the necessary data/test cases for the assignment problem. Ensure any additional CSV file follows the specified format.

The code can be compiled using the code:  
``javac CourseAllocation.java``

The code can be run using the code:  
``java CourseAllocation``

After the code is run, the assignments for the courses can be seen in the output file (output.txt).

The code shall generate a different output each time it runs, i.e., it shall give a different allocation each time while adhering to the constraints provided.

# Contributing
Contributions are welcome! You can contribute by adding more real-life constraints to enhance the problem model.  Potential modifications may include adjusting the maximum number of courses "y" for each category of professors instead of requiring exact adherence or extending the number of professor categories beyond the existing three to devise a more generalized solution. Feel free to submit issues or pull requests.

# License
This project is licensed under the MIT License - see the LICENSE.md file for details.


