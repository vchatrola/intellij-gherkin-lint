<!-- Plugin description -->
# GherkinLint: Validate Your BDD Feature Files (IntelliJ Plugin)
*GherkinLint* is an IntelliJ plugin that empowers you to write high-quality and consistent BDD feature files in Gherkin. It leverages the capabilities of Gemini, a large language model, to perform comprehensive grammar validation based on established standards.

## Features:

- **Effortless Validation:** Lint your Gherkin syntax for accuracy and identify potential issues with ease.
- **Actionable Insights:** Receive targeted suggestions for improvement based on best practices.
- **Enhanced Maintainability:** Boost the readability and maintainability of your feature files.
- **Confidence in Automation:** Strengthen your faith in the reliability of your BDD automation suite.

## Benefits:

- **Early Error Detection:** Save valuable time and effort by catching errors early in the development cycle.
- **Clean & Consistent Gherkin:** Craft cleaner and more consistent Gherkin syntax.
- **Improved Readability:** Ensure your feature files are well-structured and easy to understand for everyone involved.

## Getting Started:

1. Install GherkinLint from the JetBrains Marketplace.
2. Select the Gherkin grammar on your feature file.
3. GherkinLint will automatically analyze the file and highlight any issues.
4. Review the suggestions and make necessary corrections.

## Who Should Use This Plugin?

GherkinLint is a valuable tool for anyone who utilizes BDD frameworks in software development:

- QA Engineers
- Software Developers
- Product Managers
- Business Analysts

## Installation Guide

### Installing GherkinLint Plugin in IntelliJ with ZIP File

Follow these steps to install the *GherkinLint* plugin in IntelliJ using the provided ZIP file:

1. **Download the ZIP File**: [Download the latest version](path/to/your/plugin.zip)

2. **Open IntelliJ IDEA**: Launch IntelliJ IDEA on your computer.

3. **Navigate to the Plugin Settings**:
    - Go to `File` > `Settings` (on Windows/Linux) or `IntelliJ IDEA` > `Preferences` (on macOS).
    - In the left pane, select `Plugins`.

4. **Install Plugin from Disk**:
    - Click on the `⚙️` (gear icon) at the top right corner of the plugins window.
    - Choose `Install Plugin from Disk`.

5. **Select the ZIP File**:
    - In the file chooser dialog, navigate to the location where you downloaded the ZIP file.
    - Select the ZIP file and click `OK`.

6. **Restart IntelliJ IDEA**:
    - After the plugin is installed, IntelliJ IDEA will prompt you to restart.
    - Click `Restart IDE` to complete the installation.

7. **Verify Installation**:
    - After restarting, go to `File` > `Settings` (on Windows/Linux) or `IntelliJ IDEA` > `Preferences` (on macOS).
    - Select `Plugins` and verify that *GherkinLint* is listed among the installed plugins.

Now you're ready to use *GherkinLint* to validate your Gherkin feature files in IntelliJ IDEA!

### Setting Up `GOOGLE_API_KEY` Environment Variable

#### For Windows

1. **Open System Properties**:
   - Press `Win + X` to open the Power User menu.
   - Select `System`.
   - Click on `Advanced system settings` on the left side.

2. **Open Environment Variables**:
   - In the System Properties window, click on the `Environment Variables` button.

3. **Create a New System Variable**:
   - In the Environment Variables window, click on the `New` button under the `System variables` section.

4. **Set the Variable Name and Value**:
   - In the New System Variable dialog, enter `GOOGLE_API_KEY` as the variable name.
   - Enter your API token as the variable value.
   - Click `OK` to save the new variable.

5. **Apply the Changes**:
   - Click `OK` in the Environment Variables window.
   - Click `OK` in the System Properties window to apply the changes.

#### For macOS

1. **Open Terminal**:
   - Press `Cmd + Space` to open Spotlight Search.
   - Type `Terminal` and press Enter.

2. **Open the Profile File**:
   - Depending on your shell, open your profile file in a text editor.
   - For `bash`, use: `nano ~/.bash_profile`
   - For `zsh`, use: `nano ~/.zshrc`

3. **Set the Environment Variable**:
   - Add the following line to the file:
     ```sh
     export GOOGLE_API_KEY="your_api_token"
     ```

4. **Apply the Changes**:
   - Save the file and exit the text editor (`Ctrl + X` for nano).
   - Run the following command to apply the changes:
     ```sh
     source ~/.bash_profile  # for bash
     source ~/.zshrc  # for zsh
     ```

Now the `GOOGLE_API_KEY` environment variable is set up and ready to be used by your IntelliJ plugin.

### Getting a Free Gemini API Key from Google

To use the Gemini API with GherkinLint, you need to obtain a free API key from Google. Follow these steps to get your API key:

1. **Go to the Google Cloud Console**:
   - Open your web browser and go to the [Google Cloud Console](https://console.cloud.google.com/).

2. **Create a New Project**:
   - Click on the project dropdown at the top of the page.
   - Click `New Project` and enter a name for your project.
   - Click `Create` to create the project.

3. **Enable the Gemini API**:
   - With your project selected, go to the [API Library](https://console.cloud.google.com/apis/library).
   - Search for `Gemini API`.
   - Click on the Gemini API and then click `Enable`.

4. **Create Credentials**:
   - Go to the [Credentials](https://console.cloud.google.com/apis/credentials) page.
   - Click `Create credentials` and select `API key`.
   - Copy the API key that is generated.

5. **Set the API Key as an Environment Variable**:
   - Follow the instructions in the previous section to set the `GOOGLE_API_KEY` environment variable on your operating system.

You now have a free Gemini API key from Google and can use it with GherkinLint.

## Contribute to GherkinLint:

We actively encourage contributions from the community! Feel free to fork the repository, fix bugs, add features, or enhance the documentation.
<!-- Plugin description end -->
