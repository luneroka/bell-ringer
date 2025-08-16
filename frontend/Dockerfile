FROM node:20

# Set working directory
WORKDIR /workspace/frontend

# Copy package files first for better caching
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy project files
COPY . .

# Expose port for Vite dev server
EXPOSE 5173

# Start the development server with proper configuration
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5173"]