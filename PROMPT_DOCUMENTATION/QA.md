# QA - Prompts - GIthub Copilot /ClaudeHaiku 4.5 - 2026-02-06


1. **Roadmap QA Engineer**  
   **Prompt**: I am working in a full cycle software development project .I would be the QA Engineer.I should validate the acceptance criteria,security of the code generated and stress tests. My team has agreed on using Github Copilot claude haiku 4.5 for the whole process. How can I accomplish my role implementing Github coilot efficiently on the development from my tasks as QA Engineer. Could you give me an outline or roadmap on how to carry my role satisfactory? 
   **Analysis**: This prompt is clear and concise, making it easy to understand the user's intent.  
  
2. **AI Workflow**  
   **Prompt**: I want you to create the markdown with the following structure: 1. Methodology 2.Key Interactions3. Key Documents and context 4.Interaction dynamics .For methodology we hace selected Scrum and DevOps .Would these be accurate for a project that just requires 2 days and is about the development of an app?. For key interactions are okay the ones you suggested except for  refactoring and code review maybe reformulate as code improvement. For Key Documents add waht you suggested earlier.On the dynamics Join these link that will determine how we are going to carry out prompt engineering and modify the section Knowledge sharing specifying daily meeting 2  hours long . to structure and set up for beginning, Code and develop and finally test and add the conclusion of the project
   **Analysis**: Clearly specifies the structure and content of each section. Prompts focusing on specific files yield better results.  

3. **Debugging and Workflow Creation**
  **Prompt**:The workflow failed in each step https://github.com/majoymajo/Taller_Diagnostico/actions/runs/21729111699.I think that it is because it is not approching the right files it is reviewing the main but what it needs to review is all branches with feature/* .And if the code passes the test approve the pullre quest other wise send the files in case of bugs to Github issues or write a feedback and reject the PR
Annotations
2 errors
Validate Code Quality
No file in /home/runner/work/Taller_Diagnostico/Taller_Diagnostico matched to [**/pom.xml], make sure you have checked out the target repositoryhttps://github.com/majoymajo/Taller_Diagnostico/pull/6
Send Notifications & Generate Report
Unhandled error: HttpError: Resource not accessible by integration.
I want you to fix each part of the code that when the github action is executed it passes all the steps.
  **Analysis**: This prompt provided the exact bugs presented and even links to give clear context of the problem.
