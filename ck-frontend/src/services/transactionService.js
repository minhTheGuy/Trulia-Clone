import axios from 'axios';
import authHeader from './authHeader';

const API_URL = 'http://localhost:8080/api/transactions/';

const transactionService = {
  createRentalTransaction: async (data) => {
    const response = await axios.post(API_URL + 'create-rental', data, { headers: authHeader() });
    return response.data;
  }
};

export default transactionService; 